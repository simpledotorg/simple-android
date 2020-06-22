package org.simple.clinic.home

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import javax.inject.Inject

typealias Ui = HomeScreen
typealias UiChange = (Ui) -> Unit

class HomeScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userClock: UserClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        screenSetup(replayedEvents),
        changeFacility(replayedEvents))
  }

  private fun screenSetup(events: Observable<UiEvent>): Observable<UiChange> {
    val facilityStream = facilityStream(events)
        .replay()
        .refCount()

    val overdueAppointmentCountStream = overdueAppointmentCountStream(facilityStream)

    val homeFacilityNameStream = facilityStream
        .map { facility -> { ui: Ui -> ui.setFacility(facility.name) } }

    val homeOverdueAppointmentCountStream = homeOverdueAppointmentCountStream(overdueAppointmentCountStream)

    return Observable.merge(homeFacilityNameStream, homeOverdueAppointmentCountStream)
  }

  private fun homeOverdueAppointmentCountStream(overdueAppointmentCountStream: Observable<Int>): Observable<(Ui) -> Unit>? {
    return overdueAppointmentCountStream
        .map { overdueCount ->
          { ui: Ui ->
            if (overdueCount > 0) {
              ui.showOverdueAppointmentCount(overdueCount)
            } else {
              ui.removeOverdueAppointmentCount()
            }
          }
        }
  }

  private fun overdueAppointmentCountStream(facilityStream: Observable<Facility>): Observable<Int> {
    return facilityStream
        .flatMap { currentFacility ->
          appointmentRepository.overdueAppointmentsCount(
              since = LocalDate.now(userClock),
              facility = currentFacility
          )
        }
  }

  private fun facilityStream(events: Observable<UiEvent>): Observable<Facility> {
    return events.ofType<ScreenCreated>()
        .flatMap {
          userSession
              .requireLoggedInUser()
              .switchMap {
                facilityRepository.currentFacility(it)
              }
        }
  }

  private fun changeFacility(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<HomeFacilitySelectionClicked>()
        .map { { ui: Ui -> ui.openFacilitySelection() } }
  }
}
