package org.simple.clinic.summary.medicalhistory

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.summary.PatientSummaryScreenUi
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.UUID

// TODO(vs): 2020-01-07 Change to screen specific types
typealias Ui = PatientSummaryScreenUi

typealias UiChange = (Ui) -> Unit

class MedicalHistorySummaryUiController(
    private val patientUuid: UUID,
    private val repository: MedicalHistoryRepository,
    private val clock: UtcClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return Observable.merge(
        displayMedicalHistory(events),
        updateMedicalHistory(events)
    )
  }

  private fun displayMedicalHistory(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .switchMap { repository.historyForPatientOrDefault(patientUuid) }
        .map { { ui: Ui -> ui.medicalHistorySummaryUi().populateMedicalHistory(it) } }
  }

  private fun updateMedicalHistory(events: Observable<UiEvent>): Observable<UiChange> {
    val medicalHistories = repository.historyForPatientOrDefault(patientUuid)

    return events.ofType<SummaryMedicalHistoryAnswerToggled>()
        .withLatestFrom(medicalHistories)
        .map { (toggleEvent, medicalHistory) -> medicalHistory.answered(toggleEvent.question, toggleEvent.answer) }
        .flatMap { medicalHistory ->
          repository
              .save(medicalHistory, Instant.now(clock))
              .andThen(Observable.never<UiChange>())
        }
  }
}
