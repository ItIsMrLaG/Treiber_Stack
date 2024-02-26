import java.lang.RuntimeException
import java.lang.System.currentTimeMillis
import java.util.concurrent.atomic.AtomicReference

class TreiberStack<T>(
    private val arrSize: Int,
    private val pushSleepMs: Int,
    private val maxAttempts: Int,
    private val popFromStackAttemptsMax: Int,
) {

    private enum class States {

        EMPTY, WIP, WAIT
    }

    private class Node<T>(val value: T?, val next: Node<T>?)
    private class Exchanger<T> {

        private val state = AtomicReference(States.EMPTY)
        var value: T? = null

        fun tryUpdateState(oldState: States, newState: States): Boolean {
            return state.compareAndSet(oldState, newState)
        }

        fun updateState(oldState: States, newState: States) {
            if (!tryUpdateState(
                    oldState,
                    newState
                )
            ) throw RuntimeException("Unexpected status change")
        }

        fun removeNode(): Node<T>? {
            this.value = null
            return this.value
        }

        fun updateNode(new: T) {
            this.value = new
        }
    }

    /*   ***************** Fields *****************    */

    private val h = AtomicReference<Node<T>?>(null)

    private val eliminationArray: Array<Exchanger<T>>


    /*   ***************** Internal *****************    */

    init {
        if (arrSize < 2) throw RuntimeException("Array size must be greater then 2 or equal 2")
        eliminationArray = Array(size = arrSize) { Exchanger() }
    }

    private fun getInd(right: Int) = (0..<right).random()

    private fun sleep(delta: Int) {
        val end = currentTimeMillis() + delta
        while (currentTimeMillis() < end) continue
        return
    }

    /*   ***************** GET *****************    */

    fun read(): T? = h.get()?.value

    /*   ***************** PUSH *****************    */

    private fun tryPush(value: T): Boolean {
        val head = h.get()
        val node = Node(value, head)
        return h.compareAndSet(head, node)
    }

    private fun tryPushEliminate(value: T): Boolean {

        fun initExchanger(exc: Exchanger<T>) {
            exc.value = value
            exc.updateState(
                States.WIP,
                States.WAIT
            )
        }

        fun freeExchanger(exc: Exchanger<T>): Boolean {
            if (exc.tryUpdateState(States.WAIT, States.WIP)) {
                exc.value = null
                exc.updateState(
                    States.WIP,
                    States.EMPTY
                )
                return false
            }
            return true
        }

        var attempts = 0

        while (attempts != maxAttempts) {
            val exc = eliminationArray[getInd(arrSize)]

            if (exc.tryUpdateState(States.EMPTY, States.WIP)) {
                initExchanger(exc)
                sleep(pushSleepMs)
                return freeExchanger(exc)
            }
            attempts++
        }
        return false
    }

    fun push(value: T) {
        while (true) {
            if (tryPush(value)) return
            if (tryPushEliminate(value)) return
        }
    }

    /*   ***************** POP *****************    */

    private fun tryPop(): Pair<Node<T>?, Boolean> {
        val head = h.get()
        if (h.compareAndSet(head, head?.next)) {
            return Pair(head, true)
        }
        return Pair(null, false)
    }

    private fun tryPopEliminate(): Node<T>? {

        fun eliminateOperation(exc: Exchanger<T>): T? {
            val res = exc.value
            exc.value = null
            exc.updateState(
                States.WIP,
                States.EMPTY
            )
            return res
        }

        var attempts = 0

        while (attempts != maxAttempts) {
            val exc = eliminationArray[getInd(arrSize)]

            if (exc.tryUpdateState(States.WAIT, States.WIP)) {
                return Node(eliminateOperation(exc), null)
            }

            attempts++
        }
        return null
    }

    fun pop(): T? {
        var counter = 0

        while (true) {
            val (node, casRes) = tryPop()

            if (casRes) {
                node?.let {
                    return it.value
                }
                if (counter == popFromStackAttemptsMax) throw RuntimeException("Trying to remove an element from an empty stack")

                counter++
                sleep(pushSleepMs)
            }

            tryPopEliminate()?.let {
                return it.value
            }
        }
    }
}
