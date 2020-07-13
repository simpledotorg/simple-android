package org.simple.clinic.recentpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

typealias Ui = AllRecentPatientsUi
typealias UiChange = (Ui) -> Unit

class RecentPatientsScreenController @Inject constructor(
    private val userSession: UserSession,
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val userClock: UserClock,
    @Named("full_date") private val dateFormatter: DateTimeFormatter
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        showRecentPatients(replayedEvents),
        openPatientSummary(replayedEvents)
    )
  }

  private fun showRecentPatients(events: Observable<UiEvent>): Observable<UiChange> =
      events.ofType<ScreenCreated>()
          .flatMap { userSession.loggedInUser() }
          .filterAndUnwrapJust()
          .switchMap { facilityRepository.currentFacility(it) }
          .switchMap { facility ->
            patientRepository.recentPatients(facility.uuid)
          }
          .map {
            val today = LocalDate.now(userClock)
            it.map { recentPatient ->
              recentPatientItem(recentPatient, today)
            }
          }
          .map { { ui: Ui -> ui.updateRecentPatients(it) } }

  private fun recentPatientItem(recentPatient: RecentPatient, today: LocalDate): RecentPatientItem {
    val patientRegisteredOnDate = recentPatient.patientRecordedAt.toLocalDateAtZone(userClock.zone)
    val isNewRegistration = today == patientRegisteredOnDate

    return RecentPatientItem(
        uuid = recentPatient.uuid,
        name = recentPatient.fullName,
        age = age(recentPatient),
        gender = recentPatient.gender,
        lastSeen = recentPatient.updatedAt,
        dateFormatter = dateFormatter,
        clock = userClock,
        isNewRegistration = isNewRegistration
    )
  }

  private fun age(recentPatient: RecentPatient): Int {
    return DateOfBirth.fromRecentPatient(recentPatient, userClock).estimateAge(userClock)
  }

  private fun openPatientSummary(events: Observable<UiEvent>): ObservableSource<UiChange> =
      events
          .ofType<RecentPatientItemClicked>()
          .map { { ui: Ui -> ui.openPatientSummary(it.patientUuid) } }
}
