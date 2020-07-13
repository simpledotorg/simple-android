package org.simple.clinic.recentpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.recentpatientsview.RecentPatientItemType
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
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
          .map { RecentPatientItemType.create(it, userClock, dateFormatter) }
          .map { { ui: Ui -> ui.updateRecentPatients(it) } }

  private fun openPatientSummary(events: Observable<UiEvent>): ObservableSource<UiChange> =
      events
          .ofType<RecentPatientItemClicked>()
          .map { { ui: Ui -> ui.openPatientSummary(it.patientUuid) } }
}
