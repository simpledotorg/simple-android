package org.simple.clinic.medicalhistory.newentry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = NewMedicalHistoryScreen
typealias UiChange = (Ui) -> Unit

class NewMedicalHistoryScreenController @Inject constructor(
    private val repository: MedicalHistoryRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        saveMedicalHistory(replayedEvents))
  }

  private fun saveMedicalHistory(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<NewMedicalHistoryScreenCreated>()
        .map { it.patientUuid }

    return events
        .ofType<SaveMedicalHistoryClicked>()
        .withLatestFrom(patientUuids)
        .map { (_, uuid) -> { ui: Ui -> ui.openPatientSummaryScreen(uuid) } }
  }
}
