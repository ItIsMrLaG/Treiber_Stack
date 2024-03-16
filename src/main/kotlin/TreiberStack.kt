import kotlinx.atomicfu.*
import java.lang.RuntimeException
import java.util.*
import kotlin.math.absoluteValue

class TreiberStack<T>(
    private val arrSize: Int,
    private val maxAttempts: Int,
) : Stack<T> {

    /*   ***************** Fields *****************    */

    private val h = atomic<Node<T>?>(null)

    private val eliminationArray: AtomicArray<T?>

    private val random = Random()

    /*   ***************** Internal *****************    */

    init {
        if (arrSize < 2) throw RuntimeException("Array size must be greater then 2 or equal 2")
        eliminationArray = atomicArrayOfNulls(size = arrSize)
    }

    private fun getInd(right: Int) = (random.nextInt() % right).absoluteValue

    /*   ***************** GET *****************    */

    override fun top(): T? = h.value?.value

    /*   ***************** PUSH *****************    */

    private fun tryPush(value: T): Boolean {
        val head = h.value
        val node = Node(value, head)
        return h.compareAndSet(head, node)
    }

    private fun tryPushEliminate(value: T): Boolean {
        var attempts = 0

        while (attempts != maxAttempts) {
            val exc = eliminationArray[getInd(arrSize)]

            if (exc.compareAndSet(null, value)) {
                for (i in 0..10000) {
                }
                return !exc.compareAndSet(value, null)
            }
            attempts++
        }
        return false
    }

    override fun push(value: T) {
        while (true) {
            if (tryPush(value)) return
            if (tryPushEliminate(value)) return
        }
    }

    /*   ***************** POP *****************    */

    private fun tryPop(): Pair<Node<T>?, Boolean> {
        val head = h.value
        if (h.compareAndSet(head, head?.next)) {
            return Pair(head, true)
        }
        return Pair(null, false)
    }

    private fun tryPopEliminate(): T? {
        var attempts = 0

        while (attempts != maxAttempts) {
            val exc = eliminationArray[getInd(arrSize)]
            val value = exc.value

            if (value != null && exc.compareAndSet(value, null)) {
                return value
            }

            attempts++
        }
        return null
    }

    override fun pop(): T? {
        while (true) {
            val (node, casRes) = tryPop()

            if (casRes) {
                node?.let {
                    return it.value
                }

                return null
            }

            tryPopEliminate()?.let {
                return it
            }
        }
    }
}
