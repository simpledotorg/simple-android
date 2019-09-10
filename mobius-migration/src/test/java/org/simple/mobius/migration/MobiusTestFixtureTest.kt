package org.simple.mobius.migration

import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.MoreExecutors
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.simple.mobius.migration.fix.CounterEvent
import org.simple.mobius.migration.fix.CounterEvent.Increment
import org.simple.mobius.migration.fix.update

class MobiusTestFixtureTest {
  @Test
  fun `it can dispatch events to update the model`() {
    // given
    val executorService = MoreExecutors.newDirectExecutorService()
    val events = PublishSubject.create<CounterEvent>()
    val fixture = MobiusTestFixture(0, ::update, events, executorService)

    // when
    with(events) {
      onNext(Increment)
      onNext(Increment)
    }

    // then
    assertThat(fixture.model)
        .isEqualTo(2)
  }
}
