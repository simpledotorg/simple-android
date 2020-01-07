package org.simple.clinic.summary.medicalhistory

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.*
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

    val updateHistory = { medicalHistory: MedicalHistory, question: MedicalHistoryQuestion, answer: Answer ->
      when (question) {
        DIAGNOSED_WITH_HYPERTENSION -> medicalHistory.copy(diagnosedWithHypertension = answer)
        IS_ON_TREATMENT_FOR_HYPERTENSION -> medicalHistory.copy(isOnTreatmentForHypertension = answer)
        HAS_HAD_A_HEART_ATTACK -> medicalHistory.copy(hasHadHeartAttack = answer)
        HAS_HAD_A_STROKE -> medicalHistory.copy(hasHadStroke = answer)
        HAS_HAD_A_KIDNEY_DISEASE -> medicalHistory.copy(hasHadKidneyDisease = answer)
        HAS_DIABETES -> medicalHistory.copy(hasDiabetes = answer)
      }
    }

    return events.ofType<SummaryMedicalHistoryAnswerToggled>()
        .withLatestFrom(medicalHistories)
        .map { (toggleEvent, medicalHistory) ->
          updateHistory(medicalHistory, toggleEvent.question, toggleEvent.answer)
        }
        .flatMap {
          repository
              .save(it)
              .andThen(Observable.never<UiChange>())
        }
  }
}
