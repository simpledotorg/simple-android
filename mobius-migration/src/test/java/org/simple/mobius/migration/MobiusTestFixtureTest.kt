package org.simple.mobius.migration

import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.MoreExecutors
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.simple.mobius.migration.fix.CounterEvent
import org.simple.mobius.migration.fix.CounterEvent.Increment
import org.simple.mobius.migration.fix.CounterModel
import org.simple.mobius.migration.fix.update
import kotlin.properties.Delegates

class MobiusTestFixtureTest {
  private val executorService = MoreExecutors.newDirectExecutorService()
  private val events = PublishSubject.create<CounterEvent>()
  private val noopRenderFunction: (CounterModel) -> Unit = { /* deliberately no-op */ }

  @Test
  fun `it can dispatch events to update the model`() {
    // given
    val fixture = MobiusTestFixture(events, ::update, 0, noopRenderFunction, executorService)

    // when
    with(events) {
      onNext(Increment)
      onNext(Increment)
    }

    // then
    assertThat(fixture.model)
        .isEqualTo(2)
  }

  @Test
  fun `it can invoke the render function after a model update`() {
    // given
    val view = object {
      var model by Delegates.notNull<Int>()

      fun render(model: CounterModel) {
        this.model = model
      }
    }
    val renderFunction = { model: CounterModel -> view.render(model) }
    MobiusTestFixture(events, ::update, 0, renderFunction, executorService)

    // when
    with(events) {
      onNext(Increment)
    }

    // then
    assertThat(view.model)
        .isEqualTo(1)
  }
}
