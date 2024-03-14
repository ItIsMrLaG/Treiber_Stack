open class SequentialStackInt : Stack<Int> {

    private var h: Node<Int>? = null

    override fun pop(): Int? {
        h?.let {
            SequentialStack@ h = it.next
            return it.value
        }
        return null
    }

    override fun top(): Int? = h?.value

    override fun push(value: Int) {
        h = Node(value, h)
    }

}
