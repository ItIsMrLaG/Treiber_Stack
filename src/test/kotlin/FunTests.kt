import kotlinx.coroutines.*
import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.*
import org.jetbrains.kotlinx.lincheck.strategy.stress.*
import org.junit.jupiter.api.Test
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import kotlin.system.measureTimeMillis

class StandCase(val threads: Int, val numPushOps: Int, val numPopOps: Int, val numTopOps: Int)

object Stand {

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    suspend fun runAndMeasureTime(threads: Int, action: suspend () -> Unit) = measureTimeMillis {
        coroutineScope { // scope for coroutines
            var threadName = 0
            repeat(threads) {
                threadName += 1
                launch(newSingleThreadContext(threadName.toString())) {
                    action()
                }
            }
        }
    }

    fun <T> runCase(stack: Stack<T>, cfg: StandCase, pushVal: T): Long {
        fun op(idx: Int) = when (idx) {
            0 -> {
                val t = stack.pop()
            }

            1 -> {
                stack.push(pushVal)
            }

            else -> {
                val t = stack.top()
            }
        }

        fun action() {
            val localCfg = mutableListOf(cfg.numPopOps, cfg.numTopOps, cfg.numPushOps)
            for (i in 0 until localCfg.sum()) {
                val idx = (0..<3).random().let {
                    if (localCfg[it] != 0) return@let it
                    if (localCfg[(it + 1) % 3] != 0) return@let (it + 1)
                    return@let (it + 2)
                } % 3

                op(idx)
                localCfg[idx] -= 1
            }
        }

        var time = 0L
        runBlocking {
            withContext(Dispatchers.Default) {
                time = runAndMeasureTime(100) {
                    action()
                }
            }
        }
        return time
    }
}


@Suppress("UNUSED")
class BasicCounterTest {

    private val c = TreiberStack<Int>(arrSize = 4, maxAttempts = 3)

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


    @Test
    fun modelTest() =
        ModelCheckingOptions()
            .iterations(50)
            .invocationsPerIteration(30_000)
            .threads(6)
            .actorsPerThread(3)
            .sequentialSpecification(SequentialStackInt::class.java)
            .checkObstructionFreedom()
            .logLevel(LoggingLevel.INFO)
            .check(this::class)

    @Test
    fun performanceTest() {
        /*
10^1000 | seq = 58 | mult = 44
10^10000 | seq = 518 | mult = 363
10^100000 | seq = 6324 | mult = 3021
10^1000000 | seq = 66482 | mult = 33252
        *
        * */
        var k = 10
        for (p in 2..6) {
            k *= 10
            var R1 = 0L
            var R2 = 0L
            for (n in 1..10) {
                val trieber = TreiberStack<Int>(arrSize = 8, maxAttempts = 3)
                val sequ = SequentialStackInt()
                val r1 = Stand.runCase(
                    stack = trieber,
                    StandCase(
                        threads = 8,
                        numPushOps = k,
                        numPopOps = k,
                        numTopOps = k
                    ), 100
                )
                R1 = (R1 * (n - 1) + r1) / n
                val r2 =
                    Stand.runCase(
                        stack = sequ,
                        StandCase(
                            threads = 1,
                            numPushOps = 8 * k,
                            numPopOps = 8 * k,
                            numTopOps = 8 * k
                        ), 100
                    )
                R2 = (R2 * (n - 1) + r2) / n
            }
            println("10^${k} | seq = ${R2} | mult = ${R1}")
        }
    }
}

