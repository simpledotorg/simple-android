package org.simple.clinic.recentpatientsview

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

typealias Ui = LatestRecentPatientsUi
typealias UiChange = (Ui) -> Unit

class RecentPatientsViewController @Inject constructor(
    private val userSession: UserSession,
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val userClock: UserClock,
    private val patientConfig: PatientConfig,
    @Named("full_date") private val dateFormatter: DateTimeFormatter
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        showRecentPatients(replayedEvents),
        openPatientSummary(replayedEvents),
        openRecentPatientsScreen(replayedEvents)
    )
  }

  private fun showRecentPatients(events: Observable<UiEvent>): Observable<UiChange> {
    val currentFacilityStream = events.ofType<ScreenCreated>()
        .flatMap { userSession.loggedInUser() }
        .filterAndUnwrapJust()
        .switchMap { facilityRepository.currentFacility(it) }
        .replay()
        .refCount()

    val recentPatientsStream = currentFacilityStream
        .switchMap { facility ->
          // Fetching an extra recent patient to know whether we have more than "recentPatientLimit" number of recent patients
          patientRepository.recentPatients(
              facilityUuid = facility.uuid,
              limit = patientConfig.recentPatientLimit + 1
          )
        }

    val toggleEmptyState = recentPatientsStream
        .map { it.isNotEmpty() }
        .map { hasRecentPatients ->
          { ui: Ui -> ui.showOrHideRecentPatients(isVisible = hasRecentPatients) }
        }

    return toggleEmptyState
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<RecentPatientItemClicked>()
          .map { { ui: Ui -> ui.openPatientSummary(it.patientUuid) } }

  private fun openRecentPatientsScreen(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<SeeAllItemClicked>()
          .map { { ui: Ui -> ui.openRecentPatientsScreen() } }
}
