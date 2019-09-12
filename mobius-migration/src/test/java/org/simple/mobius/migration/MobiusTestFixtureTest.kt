package org.simple.mobius.migration

import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.MoreExecutors
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Test
import org.simple.mobius.migration.fix.CounterEffect
import org.simple.mobius.migration.fix.CounterEvent
import org.simple.mobius.migration.fix.CounterEvent.Decrement
import org.simple.mobius.migration.fix.CounterEvent.Increment
import org.simple.mobius.migration.fix.CounterModel
import org.simple.mobius.migration.fix.update
import kotlin.properties.Delegates

class MobiusTestFixtureTest {
  private val events = PublishSubject.create<CounterEvent>()
  private val defaultModel: CounterModel = 0
  private val view = View()
  private val renderFunction = { model: CounterModel -> view.render(model) }
  private val effectHandler = RxMobius
      .subtypeEffectHandler<CounterEffect, CounterEvent>()
      .addAction(CounterEffect.NegativeNumberEffect::class.java) { view.notifyNegativeNumber() }
      .build()
  private val executorService = MoreExecutors.newDirectExecutorService()
  private val fixture = MobiusTestFixture(events, ::update, defaultModel, renderFunction, effectHandler, executorService)

  @After
  fun teardown() {
    fixture.dispose()
  }

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

  @Test
  fun `it can realize effects`() {
    // when
    events.onNext(Decrement)

    // then
    assertThat(fixture.model)
        .isEqualTo(-1)
    assertThat(view.notifyNegativeNumberInvoked)
        .isTrue()
  }

  @Test
  fun `it can dispose the fixture once dispose is called`() {
    assertThat(view.model)
        .isEqualTo(0)

    // when
    fixture.dispose()
    events.onNext(Increment)

    // then
    assertThat(view.model)
        .isEqualTo(0)
  }
}

class View {
  var model by Delegates.notNull<CounterModel>()
  var notifyNegativeNumberInvoked: Boolean = false

  fun render(model: CounterModel) {
    this.model = model
  }

  fun notifyNegativeNumber() {
    this.notifyNegativeNumberInvoked = true
  }
}
