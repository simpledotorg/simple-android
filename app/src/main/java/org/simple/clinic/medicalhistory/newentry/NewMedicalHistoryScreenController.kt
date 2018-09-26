package org.simple.clinic.medicalhistory.newentry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
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
        .filter { it.question == MedicalHistoryQuestion.NONE && it.selected }
        .map { { ui: Ui -> ui.unSelectAllAnswersExceptNone() } }
  }

  private fun unSelectNone(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<NewMedicalHistoryAnswerToggled>()
        .filter { it.question != MedicalHistoryQuestion.NONE && it.selected }
        .map { { ui: Ui -> ui.unSelectNoneAnswer() } }
  }

  private fun enableSaveButton(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<NewMedicalHistoryAnswerToggled>()
        .scan(0) { selectedAnswersCount, toggleEvent ->
          if (toggleEvent.selected) selectedAnswersCount + 1 else selectedAnswersCount - 1
        }
        .map { selectedAnswersCount -> selectedAnswersCount > 0 }
        .distinctUntilChanged()
        .map { hasAnswers -> { ui: Ui -> ui.setSaveButtonEnabled(hasAnswers) } }
  }

  private fun saveMedicalHistoryAndShowSummary(events: Observable<UiEvent>): Observable<UiChange> {
    val ongoingHistoryEntry = events
        .ofType<NewMedicalHistoryAnswerToggled>()
        .scan(OngoingMedicalHistoryEntry()) { ongoingEntry, toggleEvent ->
          val question = toggleEvent.question
          val selected = toggleEvent.selected
          question.setter(ongoingEntry, selected)
        }

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
