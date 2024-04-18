package EliminationStack

import kotlin.random.Random

const val ATTEMPTS = 3

class EliminationArray<T>(private val size: Int) {

    private val array = Array(size) { Exchanger<T>(ATTEMPTS) }

    operator fun get(value: T?): Result<T?> = array[Random.nextInt(size)].exchange(value)
}
