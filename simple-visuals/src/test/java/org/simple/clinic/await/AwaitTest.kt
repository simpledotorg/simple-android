package org.simple.clinic.await

import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit.MILLISECONDS

class AwaitTest {
  private val scheduler = TestScheduler()

  @Test
  fun `it does not emit until the time is elapsed`() {
    // given
    val await = Await(listOf(10), scheduler)
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
    val await = Await(listOf(10), scheduler)
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
    val await = Await(listOf(10, 20), scheduler)
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
    val await = Await(listOf(0), scheduler)
    val testObserver = await.events().test()

    // when
    scheduler.triggerActions()

    // then
    testObserver
        .assertValues(Unit)
        .assertNoErrors()
        .assertTerminated()
  }
}
