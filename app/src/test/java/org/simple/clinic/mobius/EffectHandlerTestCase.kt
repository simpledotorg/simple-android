package org.simple.clinic.mobius

import io.reactivex.ObservableTransformer
import io.reactivex.subjects.PublishSubject

class EffectHandlerTestCase<F, E>(effectHandler: ObservableTransformer<F, E>) {

  private val incomingEffectsSubject = PublishSubject.create<F>()
  private val effectHandlerObservable = incomingEffectsSubject.compose(effectHandler)
  private val outgoingEffectsTestObserver = effectHandlerObservable.test()

  fun dispatch(effect: F) {
    incomingEffectsSubject.onNext(effect)
  }

  fun assertOutgoingEvents(vararg events: E) {
    outgoingEffectsTestObserver
        .assertValues(*events)
        .assertNoErrors()
        .assertNotTerminated()
  }

  fun assertNoOutgoingEvents() {
    outgoingEffectsTestObserver
        .assertNoValues()
        .assertNoErrors()
        .assertNotTerminated()
  }

  fun dispose() {
    outgoingEffectsTestObserver.dispose()
  }
}
