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
        openPatientSummary(replayedEvents),
        openRecentPatientsScreen(replayedEvents)
    )
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
