package org.simple.clinic.plumbing

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler

abstract class BaseUiChangeProducer<S, U>(
    private val uiScheduler: Scheduler
) : ObservableTransformer<S, (U) -> Unit> {

  override fun apply(states: Observable<S>): ObservableSource<(U) -> Unit> {
    return states
        .compose(uiChanges())
        .observeOn(uiScheduler)
  }

  abstract fun uiChanges(): ObservableTransformer<S, (U) -> Unit>
}
