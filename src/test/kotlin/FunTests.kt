import EliminationStack.TreiberStackElimination
import kotlinx.coroutines.*
import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.*
import org.jetbrains.kotlinx.lincheck.strategy.stress.*
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class StandCase(val threads: Int, val numOps: Int)

object Stand {

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    suspend fun runAndMeasureTime(threads: Int, action: suspend (Int) -> Unit) {
        val jobs = mutableListOf<Job>()
        coroutineScope { // scope for coroutines
            repeat(threads) {
                jobs.add(launch(newSingleThreadContext(it.toString())) {
                    action(it)
                })
            }
        }
        jobs.joinAll()
    }

    fun <T> runCase(stack: Stack<T>, cfg: StandCase, pushVal: T, rand: Boolean = true): Long {
        fun op(idx: Int) = when (idx) {
            0 -> {
                val t = stack.pop()
            }

            1 -> stack.push(pushVal)
            else -> {
                val t = stack.top()
            }
        }

        fun action(idx: Int) =
            repeat(cfg.numOps) { i ->
                if (rand)
                    op((i + Random.nextInt().absoluteValue) % 3)
                else op(idx % 2)
            }


        return measureTimeMillis {
            runBlocking {
                withContext(Dispatchers.Default) {
                    runAndMeasureTime(cfg.threads) { it -> action(it) }
                }
            }
        }
    }
}

class TreiberStackTest {

    private val c = TreiberStack<Int>()

    @Operation
    fun push(value: Int) = c.push(value)

    @Operation
    fun pop() = c.pop()

    @Operation
    fun top() = c.top()

    @Test
    fun stressTest() =
        StressOptions()
            .iterations(50)
            .invocationsPerIteration(50_000)
            .threads(3)
            .actorsPerThread(3)
            .sequentialSpecification(SequentialStackInt::class.java)
            .logLevel(LoggingLevel.INFO)
            .check(this::class)
}

@Suppress("UNUSED")
class TreiberStackEliminationTest {

    private val c = TreiberStackElimination<Int>(4)

    @Operation
    fun push(value: Int) = c.push(value)

    @Operation
    fun pop() = c.pop()

    @Operation
    fun top() = c.top()

    @Test
    fun stressTest() =
        StressOptions()
            .iterations(50)
            .invocationsPerIteration(50_000)
            .threads(3)
            .actorsPerThread(3)
            .sequentialSpecification(SequentialStackInt::class.java)
            .logLevel(LoggingLevel.INFO)
            .check(this::class.java)
}

class PerformanceTest {

    @Test
    fun `performance 10^6 operations per thread`() {
        val ops = 1_000_000
        for (threadN in mutableListOf(1, 2, 4, 8, 16)) {
            var elStackTime = 0L
            var stackTime = 0L

            repeat(10) { i ->
                val n = i + 1
                val elStack = TreiberStackElimination<Int>(8)
                val stack = TreiberStack<Int>()
                val standCase = StandCase(threadN, ops)

                elStackTime = (elStackTime * (n - 1) + Stand.runCase(stack = elStack, standCase, 1)) / n
                stackTime = (stackTime * (n - 1) + Stand.runCase(stack = stack, standCase, 1)) / n
            }
            println("N = $threadN | elStack = $elStackTime | stack = $stackTime")
        }
    }
}


/* рандомные
N = 1 | elStack = 19 | stack = 16
N = 2 | elStack = 40 | stack = 109
N = 4 | elStack = 107 | stack = 336
N = 8 | elStack = 327 | stack = 877
N = 16 | elStack = 1166 | stack = 1791
* */

/* подготовленные
N = 1 | elStack = 12 | stack = 10
N = 2 | elStack = 28 | stack = 126
N = 4 | elStack = 94 | stack = 469
N = 8 | elStack = 321 | stack = 1212
N = 16 | elStack = 1321 | stack = 2541
* */

