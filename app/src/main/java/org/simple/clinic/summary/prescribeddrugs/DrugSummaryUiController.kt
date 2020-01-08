package org.simple.clinic.summary.prescribeddrugs

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.summary.PatientSummaryItemChanged
import org.simple.clinic.summary.PatientSummaryScreenUi
import org.simple.clinic.widgets.UiEvent

// TODO(vs): 2020-01-08 Change to screen specific types once the refactoring is done
typealias Ui = PatientSummaryScreenUi

typealias UiChange = (Ui) -> Unit

class DrugSummaryUiController : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events
        .ofType<PatientSummaryItemChanged>()
        .map { it.patientSummaryItems.prescription }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.drugSummaryUi().populatePrescribedDrugs(it) } }
  }
}
