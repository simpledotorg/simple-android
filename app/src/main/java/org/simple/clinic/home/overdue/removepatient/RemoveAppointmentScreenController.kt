package org.simple.clinic.home.overdue.removepatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.overdue.AppointmentCancelReason.Dead
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RemoveAppointmentScreen
typealias UiChange = (Ui) -> Unit

class RemoveAppointmentScreenController @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(event: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = event.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        reasonClicks(replayedEvents),
        doneClicks(replayedEvents))
  }

  private fun reasonClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val mergedReasons = Observable.merge(
        events.ofType<CancelReasonClicked>(),
        events.ofType<PatientDeadClicked>(),
        events.ofType<PatientAlreadyVisitedClicked>())

    return mergedReasons
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.enableDoneButton() } }
  }

  private fun doneClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val appointmentUuids = events.ofType<RemoveAppointmentSheetCreated>()
        .map { it.appointmentUuid }

    val cancelReasonClicks = events.ofType<CancelReasonClicked>()
    val diedReasonClicks = events.ofType<PatientDeadClicked>()
    val patientAlreadyVisitedClicks = events.ofType<PatientAlreadyVisitedClicked>()

    val mergedReasons = Observable.merge(cancelReasonClicks, diedReasonClicks, patientAlreadyVisitedClicks)

    val doneWithLatestFromReasons = events
        .ofType<RemoveReasonDoneClicked>()
        .withLatestFrom(appointmentUuids, mergedReasons)

    val markPatientStatusDeadStream = doneWithLatestFromReasons
        .filter { (_, _, reason) -> reason is PatientDeadClicked }
        .flatMap { (_, uuid, reason) ->
          patientRepository
              .updatePatientStatusToDead((reason as PatientDeadClicked).patientUuid)
              .andThen(appointmentRepository.cancelWithReason(uuid, Dead))
              .andThen(Observable.just(Ui::closeScreen))
        }

    val markPatientAlreadyVisitedStream = doneWithLatestFromReasons
        .filter { (_, _, reason) -> reason is PatientAlreadyVisitedClicked }
        .flatMap { (_, appointmentUuid, _) ->
          appointmentRepository.markAsAlreadyVisited(appointmentUuid)
              .andThen(Observable.just(Ui::closeScreen))
        }

    val cancelWithReasonStream = doneWithLatestFromReasons
        .filter { (_, _, reason) -> reason is CancelReasonClicked }
        .flatMap { (_, uuid, reason) ->
          appointmentRepository
              .cancelWithReason(uuid, (reason as CancelReasonClicked).selectedReason)
              .andThen(Observable.just(Ui::closeScreen))
        }

    return Observable.merge(cancelWithReasonStream, markPatientStatusDeadStream, markPatientAlreadyVisitedStream)
  }
}
