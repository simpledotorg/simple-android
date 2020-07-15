package org.simple.clinic.home.overdue

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import javax.inject.Inject

typealias Ui = OverdueUi
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
        openPhoneMaskBottomSheet(replayedEvents)
    )
  }

  private fun screenSetup(events: Observable<UiEvent>): Observable<UiChange> {
    val facilityStream = events
        .ofType<OverdueScreenCreated>()
        .flatMap { userSession.loggedInUser() }
        .filterAndUnwrapJust()
        .switchMap { facilityRepository.currentFacility(it) }
        .replay()
        .refCount()

    val overdueAppointmentsStream = facilityStream
        .flatMap { currentFacility -> appointmentRepository.overdueAppointments(since = LocalDate.now(userClock), facility = currentFacility) }
        .replay()
        .refCount()

    val overduePatientsStream = overdueAppointmentsStream
        .withLatestFrom(facilityStream) { overdueAppointments, facility ->
          { ui: Ui -> ui.updateList(overdueAppointments, facility.config.diabetesManagementEnabled) }
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
}
