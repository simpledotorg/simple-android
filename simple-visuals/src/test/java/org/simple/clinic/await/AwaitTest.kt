package org.simple.clinic.await

import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit.MILLISECONDS

class AwaitTest {
  private val scheduler = TestScheduler()

  @Test
  fun `it does not emit until the time is elapsed`() {
    // given
    val singleCheckpoint = listOf(Checkpoint.unit(10))
    val await = Await(singleCheckpoint, scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.advanceTimeBy(9, MILLISECONDS)

    // then
    testObserver
        .assertNoValues()
        .assertNoErrors()
        .assertNotTerminated()
  }

  @Test
  fun `it emits immediately after the first delay`() {
    // given
    val singleCheckpoint = listOf(Checkpoint.unit(10))
    val await = Await(singleCheckpoint, scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.advanceTimeBy(10, MILLISECONDS)

    // then
    testObserver
        .assertValues(Unit)
        .assertNoErrors()
        .assertTerminated()
  }

  @Test
  fun `it can take in two delays`() {
    // given
    val checkpoints = listOf(Checkpoint.unit(10), Checkpoint.unit(20))
    val await = Await(checkpoints, scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.advanceTimeBy(20, MILLISECONDS)

    // then
    testObserver
        .assertValues(Unit, Unit)
        .assertNoErrors()
        .assertTerminated()
  }

  @Test
  fun `it emits immediately if the delay is 0`() {
    // given
    val singleCheckpoint = listOf(Checkpoint.unit(0))
    val await = Await(singleCheckpoint, scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.triggerActions()

    // then
    testObserver
        .assertValues(Unit)
        .assertNoErrors()
        .assertTerminated()
  }

  @Test
  fun `it terminates if all events are emitted`() {
    // given
    val checkpoints = listOf(Checkpoint.unit(100), Checkpoint.unit(200), Checkpoint.unit(300))
    val await = Await(checkpoints, scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.advanceTimeBy(300, MILLISECONDS)

    // then
    testObserver
        .assertValueCount(3)
        .assertNoErrors()
        .assertTerminated()
  }

  @Test
  fun `it does not terminate if all events are not emitted`() {
    // given
    val checkpoints = listOf(Checkpoint.unit(100), Checkpoint.unit(200), Checkpoint.unit(300))
    val await = Await(checkpoints, scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.advanceTimeBy(250, MILLISECONDS)

    // then
    testObserver
        .assertValueCount(2)
        .assertNoErrors()
        .assertNotTerminated()
  }

  @Test
  fun `it can emit items of any type`() {
    // given
    val checkpoints = listOf(Checkpoint("Fifty", 50), Checkpoint("Hundred", 100))
    val await = Await(checkpoints, scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.advanceTimeBy(100, MILLISECONDS)

    // then
    testObserver
        .assertValues("Fifty", "Hundred")
        .assertNoErrors()
        .assertTerminated()
  }

  @Test
  fun `it can sort items if they are not scheduled in order`() {
    // given
    val checkpoints = listOf(
        Checkpoint("One", 1),
        Checkpoint("Five", 5),
        Checkpoint("Three", 3),
        Checkpoint("Twelve", 12)
    )
    val await = Await(checkpoints, scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.advanceTimeBy(12, MILLISECONDS)

    // then
    testObserver
        .assertValues("One", "Three", "Five", "Twelve")
        .assertNoErrors()
        .assertTerminated()
  }
}
