package org.simple.mobius.migration

import com.google.common.truth.Truth.assertThat
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.mobius.migration.fix.CounterEvent
import org.simple.mobius.migration.fix.CounterEvent.Decrement
import org.simple.mobius.migration.fix.CounterEvent.Increment
import org.simple.mobius.migration.fix.CounterModel
import org.simple.mobius.migration.fix.CounterUpdate
import org.simple.mobius.migration.fix.VerifiableCounterView
import org.simple.mobius.migration.fix.createEffectHandler

class MobiusTestFixtureTest {
  private val events = PublishSubject.create<CounterEvent>()
  private val defaultModel: CounterModel = 0
  private val view = VerifiableCounterView()
  private val modelUpdateListener = { model: CounterModel -> view.render(model) }
  private val effectHandler = createEffectHandler(view)
  private val fixture = MobiusTestFixture(
      events,
      defaultModel,
      null,
      CounterUpdate(),
      effectHandler,
      modelUpdateListener
  )

  @Before
  fun setup() {
    fixture.start()
  }

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
        .isEqualTo(defaultModel)

    // when
    fixture.dispose()
    events.onNext(Increment)

    // then
    assertThat(view.model)
        .isEqualTo(defaultModel)
  }
}
