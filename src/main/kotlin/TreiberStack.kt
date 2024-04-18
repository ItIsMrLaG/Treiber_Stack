import kotlinx.atomicfu.*
import java.util.*

class TreiberStack<T>(
) : Stack<T> {

    /*   ***************** Fields *****************    */

    private val h = atomic<Node<T>?>(null)


    /*   ***************** GET *****************    */

    override fun top(): T? = h.value?.value

    /*   ***************** PUSH *****************    */

    private fun tryPush(value: T): Boolean {
        val head = h.value
        val node = Node(value, head)
        return h.compareAndSet(head, node)
    }

    override fun push(value: T) {
        while (true) {
            if (tryPush(value)) return
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

    override fun pop(): T? {
        while (true) {
            val (node, casRes) = tryPop()

            if (casRes) {
                node?.let {
                    return it.value
                }

                return null
            }
        }
    }
}
