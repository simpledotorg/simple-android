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
  private val events = PublishSubject.create<CounterEvent>()
  private val defaultModel: CounterModel = 0
  private val view = View()
  private val renderFunction = { model: CounterModel -> view.render(model) }
  private val executorService = MoreExecutors.newDirectExecutorService()
  private val fixture = MobiusTestFixture(events, ::update, defaultModel, renderFunction, executorService)

  @Test
  fun `it can dispatch events to update the model`() {
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
    // when
    events.onNext(Increment)

    // then
    assertThat(view.model)
        .isEqualTo(1)
  }
}

class View {
  var model by Delegates.notNull<CounterModel>()

  fun render(model: CounterModel) {
    this.model = model
  }
}
