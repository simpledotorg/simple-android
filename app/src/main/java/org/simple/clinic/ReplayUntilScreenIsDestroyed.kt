package org.simple.clinic

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent

class ReplayUntilScreenIsDestroyed(private val events: Observable<UiEvent>) {

  private var transforms: List<ObservableTransformer<UiEvent, UiEvent>> = emptyList()

  private val disposables = CompositeDisposable()

  fun compose(transformer: ObservableTransformer<UiEvent, UiEvent>): ReplayUntilScreenIsDestroyed {
    transforms = transforms + transformer
    return this
  }

  fun replay(): Observable<UiEvent> {
    val replayedEvents = events
        .replay()
        .autoConnect(1) { disposables += it }

    val transformedEvents = transforms
        .fold(replayedEvents) { events, transform ->
          events.compose(transform)
              .replay()
              .autoConnect(1) { disposables += it }
        }

    disposeWhenScreenIsDestroyed(transformedEvents)

    return transformedEvents
  }

  private fun disposeWhenScreenIsDestroyed(events: Observable<UiEvent>) {
    disposables.add(
        events
            .ofType<ScreenDestroyed>()
            .subscribe { disposables.clear() })
  }
}
