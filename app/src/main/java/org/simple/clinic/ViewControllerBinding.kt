package org.simple.clinic

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent

@SuppressLint("CheckResult")
fun <T> bindUiToController(
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
