import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.*
import org.jetbrains.kotlinx.lincheck.strategy.stress.*
import org.junit.jupiter.api.Test
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions

@Suppress("UNUSED")
class BasicCounterTest {
    private val c = TreiberStack<Int>(arrSize=6, maxAttempts=2)

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
//            .sequentialSpecification(SequentialIntStack::class.java)
            .logLevel(LoggingLevel.INFO)
            .check(this::class)


     @Test
    fun modelTest() =
        ModelCheckingOptions()
            .iterations(50)
            .invocationsPerIteration(30_000)
            .threads(3)
            .actorsPerThread(3)
//            .sequentialSpecification(SequentialIntStack::class.java)
            .checkObstructionFreedom()
            .logLevel(LoggingLevel.INFO)
            .check(this::class)
}

