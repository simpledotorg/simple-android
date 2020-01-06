package org.simple.clinic.summary.bloodpressures

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.widgets.UiEvent

// TODO(vs): 2020-01-06 Replace with typealiases specific to this screen once the refactor is over
typealias Ui = org.simple.clinic.summary.Ui
typealias UiChange = org.simple.clinic.summary.UiChange

class BloodPressureSummaryViewController : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): ObservableSource<UiChange> {
    return Observable.empty()
  }
}
