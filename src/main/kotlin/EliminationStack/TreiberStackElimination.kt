package EliminationStack

import Node
import Stack
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop

class TreiberStackElimination<T>(size: Int) : Stack<T> {

    private var head = atomic<Node<T>?>(null)

    private val eliminationArray = EliminationArray<T>(size)

    override fun top() = head.value?.value

    override fun pop(): T? = head.loop { hNode ->
        val nextHead = hNode?.next
        if (head.compareAndSet(hNode, nextHead)) return hNode?.value

        val alternative = eliminationArray[null]
        if (alternative.isSuccess) alternative.getOrNull()?.let { return it }
    }

    override fun push(value: T): Unit = head.loop { hNode ->
        val newHead = Node(value, hNode)
        if (head.compareAndSet(hNode, newHead)) return

        val alternative = eliminationArray[value]
        if (alternative.isSuccess && alternative.getOrNull() == null) return
    }
}
