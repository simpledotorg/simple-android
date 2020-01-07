package org.simple.clinic.summary.medicalhistory

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.summary.PatientSummaryScreenUi
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

// TODO(vs): 2020-01-07 Change to screen specific types
typealias Ui = PatientSummaryScreenUi

typealias UiChange = (Ui) -> Unit

class MedicalHistorySummaryUiController(
    private val patientUuid: UUID,
    private val repository: MedicalHistoryRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return displayMedicalHistory(events)
  }

  private fun displayMedicalHistory(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .switchMap { repository.historyForPatientOrDefault(patientUuid) }
        .map { { ui: Ui -> ui.medicalHistorySummaryUi().populateMedicalHistory(it) } }
  }
}
