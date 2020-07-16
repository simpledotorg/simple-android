package org.simple.clinic.home.overdue

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.UiEvent
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
        .replay()

    return openPhoneMaskBottomSheet(replayedEvents)
  }

  private fun openPhoneMaskBottomSheet(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<CallPatientClicked>()
          .map { { ui: Ui -> ui.openPhoneMaskBottomSheet(it.patientUuid) } }
}
