package EliminationStack

import java.util.concurrent.atomic.AtomicStampedReference

class Exchanger<T>(private val exchangeAttempts: Int) {
    companion object {

        private const val WAIT = 0
        private const val EMPTY = 1
        private const val WIP = 2
    }

    private val container = AtomicStampedReference<T>(null, EMPTY)

    fun exchange(item: T?): Result<T?> {
        val stampHolder = IntArray(1) { EMPTY }

        repeat(exchangeAttempts) {
            val currentItem = container.get(stampHolder)
            val state = stampHolder[0]

            when (state) {
                EMPTY -> {
                    if (container.compareAndSet(currentItem, item, EMPTY, WAIT)) {
                        for (i in 0..100) { /* BACK-OFF */ }

                        var newItem = container.get(stampHolder)
                        if (stampHolder[0] == WIP) {
                            container.set(null, EMPTY)
                            return Result.success(newItem)
                        }

                        if (container.compareAndSet(item, null, WAIT, EMPTY))
                            return Result.failure(Exception("Pop didn't come"))

                        newItem = container.get(stampHolder)
                        container.set(null, EMPTY)
                        return Result.success(newItem)
                    }
                }

                WAIT -> {
                    if (container.compareAndSet(currentItem, item, WAIT, WIP))
                        return Result.success(currentItem)
                }
            }
        }
        return Result.failure(Exception("The Exchanger is busy"))
    }
}
