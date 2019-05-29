package org.simple.clinic.bp.entry.confirmremovebloodpressure

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.ConfirmRemoveBloodPressureDialog
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ConfirmRemoveBloodPressureDialog
typealias UiChange = (Ui) -> Unit

class ConfirmRemoveBloodPressureDialogController @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val patientRepository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return markBloodPressureAsDeleted(replayedEvents)
  }

  private fun markBloodPressureAsDeleted(events: Observable<UiEvent>): Observable<UiChange> {
    val savedBloodPressureMeasurementUuidStream = events
        .ofType<ConfirmRemoveBloodPressureDialogCreated>()
        .map { it.bloodPressureMeasurementUuid }

    return events
        .ofType<ConfirmRemoveBloodPressureDialogRemoveClicked>()
        .withLatestFrom(savedBloodPressureMeasurementUuidStream) { _, bloodPressureMeasurementUuid -> bloodPressureMeasurementUuid }
        .flatMap { bloodPressureRepository.measurement(it) }
        .take(1)
        .flatMap {
          bloodPressureRepository
              .markBloodPressureAsDeleted(it)
              .andThen(patientRepository.updateRecordedAt(it.patientUuid))
              .andThen(Observable.just({ ui: Ui -> ui.dismiss() }))
        }
  }
}
