package org.simple.clinic.medicalhistory.newentry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.NONE
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = NewMedicalHistoryScreen
typealias UiChange = (Ui) -> Unit

class NewMedicalHistoryScreenController @Inject constructor(
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val patientRepository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        unSelectAllOnNoneSelection(replayedEvents),
        unSelectNone(replayedEvents),
        enableSaveButton(replayedEvents),
        saveMedicalHistoryAndShowSummary(replayedEvents))
  }

  private fun unSelectAllOnNoneSelection(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<NewMedicalHistoryAnswerToggled>()
        .filter { it.question == NONE && it.selected }
        .map { { ui: Ui -> ui.unSelectAllAnswersExceptNone() } }
  }

  private fun unSelectNone(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<NewMedicalHistoryAnswerToggled>()
        .filter { it.question != NONE && it.selected }
        .map { { ui: Ui -> ui.unSelectNoneAnswer() } }
  }

  private fun enableSaveButton(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<NewMedicalHistoryAnswerToggled>()
        .scan(setOf<MedicalHistoryQuestion>()) { selections, toggleEvent ->
          when {
            toggleEvent.selected -> selections + toggleEvent.question
            else -> selections - toggleEvent.question
          }
        }
        .map { it.isNotEmpty() }
        .distinctUntilChanged()
        .map { hasAnswers -> { ui: Ui -> ui.setSaveButtonEnabled(hasAnswers) } }
  }

  private fun saveMedicalHistoryAndShowSummary(events: Observable<UiEvent>): Observable<UiChange> {
    val updateEntry = { entry: OngoingMedicalHistoryEntry, toggleEvent: NewMedicalHistoryAnswerToggled ->
      toggleEvent.run {
        when (question) {
          HAS_HAD_A_HEART_ATTACK -> entry.copy(hasHadHeartAttack = selected)
          HAS_HAD_A_STROKE -> entry.copy(hasHadStroke = selected)
          HAS_HAD_A_KIDNEY_DISEASE -> entry.copy(hasHadKidneyDisease = selected)
          IS_ON_TREATMENT_FOR_HYPERTENSION -> entry.copy(isOnTreatmentForHypertension = selected)
          HAS_DIABETES -> entry.copy(hasDiabetes = selected)
          NONE -> entry
        }
      }
    }

    val ongoingHistoryEntry = events
        .ofType<NewMedicalHistoryAnswerToggled>()
        .scan(OngoingMedicalHistoryEntry(), updateEntry)

    return events
        .ofType<SaveMedicalHistoryClicked>()
        .flatMapSingle { patientRepository.saveOngoingEntryAsPatient() }
        .withLatestFrom(ongoingHistoryEntry)
        .flatMap { (savedPatient, entry) ->
          medicalHistoryRepository
              .save(savedPatient.uuid, entry)
              .andThen(Observable.just({ ui: Ui -> ui.openPatientSummaryScreen(savedPatient.uuid) }))
        }
  }
}
