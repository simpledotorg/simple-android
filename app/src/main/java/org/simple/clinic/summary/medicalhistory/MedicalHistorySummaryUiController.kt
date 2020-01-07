package org.simple.clinic.summary.medicalhistory

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.UUID

typealias Ui = MedicalHistorySummaryUi

typealias UiChange = (Ui) -> Unit

class MedicalHistorySummaryUiController @AssistedInject constructor(
    @Assisted private val patientUuid: UUID,
    private val repository: MedicalHistoryRepository,
    private val clock: UtcClock
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: UUID): MedicalHistorySummaryUiController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        displayMedicalHistory(replayedEvents),
        updateMedicalHistory(replayedEvents)
    )
  }

  private fun displayMedicalHistory(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .switchMap { repository.historyForPatientOrDefault(patientUuid) }
        .map { { ui: Ui -> ui.populateMedicalHistory(it) } }
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
