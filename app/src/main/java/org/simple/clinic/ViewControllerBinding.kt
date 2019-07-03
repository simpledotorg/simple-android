package org.simple.clinic

import android.annotation.SuppressLint
import android.view.View
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.plumbing.BaseUiChangeProducer
import org.simple.clinic.plumbing.BaseUiStateProducer
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.ScreenRestored
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
fun <T> bindUiToController(
    ui: T,
    events: Observable<UiEvent>,
    controller: ObservableTransformer<UiEvent, (T) -> Unit>,
    screenDestroys: Observable<ScreenDestroyed>,
    uiChangeDelay: Duration = SCREEN_CHANGE_ANIMATION_DURATION,
    delaySubscription: Boolean = true
) {
  events
      .mergeWith(screenDestroys)
      .observeOn(io())
      .compose(controller)
      .let { uiChanges ->
        if (delaySubscription) {
          uiChanges.delaySubscription(uiChangeDelay.toMillis(), TimeUnit.MILLISECONDS, mainThread())
        } else {
          uiChanges
        }
      }
      .observeOn(mainThread())
      .takeUntil(screenDestroys)
      .subscribe { uiChange -> uiChange(ui) }
}

// FIXME(vs) 21/Jun/19 - Revisit and try to de-dupe `bindUiToController`.
@SuppressLint("CheckResult")
fun <T> bindUiToControllerWithoutDelay(
    ui: T,
    events: Observable<UiEvent>,
    controller: ObservableTransformer<UiEvent, (T) -> Unit>,
    screenDestroys: Observable<ScreenDestroyed>
) {
  events
      .mergeWith(screenDestroys)
      .observeOn(io())
      .compose(controller)
      .observeOn(mainThread())
      .takeUntil(screenDestroys)
      .subscribe { uiChange -> uiChange(ui) }
}

class ViewControllerBinding<E, S, T>(
    private val uiStateProducer: BaseUiStateProducer<E, S>,
    private val uiChangeProducer: BaseUiChangeProducer<S, T>
) : View.OnAttachStateChangeListener {

  companion object {

    fun <E, S, T> bindToView(
        view: View,
        uiStateProducer: BaseUiStateProducer<E, S>,
        uiChangeProducer: BaseUiChangeProducer<S, T>
    ): ViewControllerBinding<E, S, T> {
      val viewControllerBinding = ViewControllerBinding(
          uiStateProducer = uiStateProducer,
          uiChangeProducer = uiChangeProducer
      )

      view.addOnAttachStateChangeListener(viewControllerBinding)

      return viewControllerBinding
    }
  }

  private var disposable: Disposable? = null

  private val uiEvents: PublishSubject<E> = PublishSubject.create()

  @Suppress("UNCHECKED_CAST")
  override fun onViewAttachedToWindow(v: View) {
    val stateSubject = uiStateProducer.states

    disposable = uiEvents
        .compose(uiStateProducer)
        .doOnNext { state -> stateSubject.onNext(state) }
        .compose(uiChangeProducer)
        .subscribe { uiChange -> uiChange(v as T) }

    val screenEvent = if (stateSubject.value == null) ScreenCreated() else ScreenRestored
    uiEvents.onNext(screenEvent as E)
  }

  override fun onViewDetachedFromWindow(v: View) {
    disposable!!.dispose()
    disposable = null
    v.removeOnAttachStateChangeListener(this)
  }

  fun onEvent(event: E) {
    uiEvents.onNext(event)
  }

  fun latestState(): S? {
    return uiStateProducer.states.value
  }

  fun restoreSavedState(state: S) {
    uiStateProducer.states.onNext(state)
  }
}
