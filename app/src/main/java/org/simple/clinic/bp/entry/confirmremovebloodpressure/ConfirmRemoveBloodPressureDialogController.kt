package org.simple.clinic.bp.entry.confirmremovebloodpressure

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.ConfirmRemoveBloodPressureDialog
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ConfirmRemoveBloodPressureDialog
typealias UiChange = (Ui) -> Unit

class ConfirmRemoveBloodPressureDialogController @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return markBloodPressureAsDeleted(replayedEvents)
  }

  private fun markBloodPressureAsDeleted(events: Observable<UiEvent>): Observable<UiChange> {
    val savedBloodPressureMeasurementStream = events
        .ofType<ConfirmRemoveBloodPressureDialogCreated>()
        .flatMapSingle { bloodPressureRepository.measurement(it.bloodPressureMeasurementUuid) }

    return events
        .ofType<ConfirmRemoveBloodPressureDialogRemoveClicked>()
        .withLatestFrom(savedBloodPressureMeasurementStream) { _, bloodPressureMeasurement -> bloodPressureMeasurement }
        .flatMap {
          bloodPressureRepository
              .markBloodPressureAsDeleted(it)
              .andThen(Observable.just({ ui: Ui -> ui.dismiss() }))
        }
  }
}
