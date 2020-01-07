package org.simple.clinic.summary.medicalhistory

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.summary.PatientSummaryScreenUi
import org.simple.clinic.widgets.UiEvent

// TODO(vs): 2020-01-07 Change to screen specific types
typealias Ui = PatientSummaryScreenUi
typealias UiChange = (Ui) -> Unit

class MedicalHistorySummaryUiController: ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): ObservableSource<UiChange> {
    return Observable.never()
  }
}
