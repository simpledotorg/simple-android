package org.simple.clinic

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
fun <T> bindUiToController(
    ui: T,
    events: Observable<UiEvent>,
    controller: ObservableTransformer<UiEvent, (T) -> Unit>,
    screenDestroys: Observable<ScreenDestroyed>,
    uiChangeDelay: Duration = SCREEN_CHANGE_ANIMATION_DURATION
) {
  events
      .mergeWith(screenDestroys)
      .observeOn(io())
      .compose(controller)
      .delaySubscription(uiChangeDelay.toMillis(), TimeUnit.MILLISECONDS)
      .observeOn(mainThread())
      .takeUntil(screenDestroys)
      .subscribe { uiChange -> uiChange(ui) }
}

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
