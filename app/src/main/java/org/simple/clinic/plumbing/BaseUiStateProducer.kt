package org.simple.clinic.plumbing

import io.reactivex.ObservableTransformer
import io.reactivex.subjects.BehaviorSubject

abstract class BaseUiStateProducer<E, S> : ObservableTransformer<E, S> {
  val states: BehaviorSubject<S> = BehaviorSubject.create()
}
