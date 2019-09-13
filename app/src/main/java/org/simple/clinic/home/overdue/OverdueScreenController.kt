package org.simple.clinic.home.overdue

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = OverdueScreen
typealias UiChange = (Ui) -> Unit

class OverdueScreenController @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val userClock: UserClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        screenSetup(replayedEvents),
        markedAsAgreedToVisit(replayedEvents),
        rescheduleAppointment(replayedEvents),
        removeAppointment(replayedEvents),
        reportViewedPatientEvent(replayedEvents),
        openPhoneMaskBottomSheet(replayedEvents)
    )
  }

  private fun reportViewedPatientEvent(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<AppointmentExpanded>()
        .doOnNext { (patientUuid) -> Analytics.reportViewedPatient(patientUuid, OverdueScreenKey().analyticsName) }
        .flatMap { Observable.empty<UiChange>() }
  }

  private fun screenSetup(events: Observable<UiEvent>): Observable<UiChange> {
    val overdueAppointmentsStream = events
        .ofType<OverdueScreenCreated>()
        .flatMap { userSession.loggedInUser() }
        .filterAndUnwrapJust()
        .switchMap { facilityRepository.currentFacility(it) }
        .flatMap { currentFacility -> appointmentRepository.overdueAppointments(currentFacility) }
        .replay()
        .refCount()

    val overduePatientsStream = overdueAppointmentsStream
        .map { overdueAppointments ->
          { ui: Ui -> ui.updateList(overdueAppointments) }
        }

    val emptyStateStream = overdueAppointmentsStream
        .map { it.isEmpty() }
        .map { { ui: Ui -> ui.handleEmptyList(it) } }

    return Observable.merge(overduePatientsStream, emptyStateStream)
  }

  private fun openPhoneMaskBottomSheet(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<CallPatientClicked>()
          .map { { ui: Ui -> ui.openPhoneMaskBottomSheet(it.patientUuid) } }

  private fun markedAsAgreedToVisit(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<AgreedToVisitClicked>()
        .flatMap {
          appointmentRepository
              .markAsAgreedToVisit(it.appointmentUuid, userClock)
              .toObservable<UiChange>()
        }
  }

  private fun rescheduleAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RemindToCallLaterClicked>()
        .map { { ui: Ui -> ui.showAppointmentReminderSheet(it.appointmentUuid) } }
  }

  private fun removeAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RemoveFromListClicked>()
        .map { { ui: Ui -> ui.showRemovePatientReasonSheet(it.appointmentUuid, it.patientUuid) } }
  }
}
