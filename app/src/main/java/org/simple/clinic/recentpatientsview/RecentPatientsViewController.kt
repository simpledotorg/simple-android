package org.simple.clinic.recentpatientsview

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

typealias Ui = RecentPatientsView
typealias UiChange = (Ui) -> Unit

class RecentPatientsViewController @Inject constructor(
    private val userSession: UserSession,
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val relativeTimestampGenerator: RelativeTimestampGenerator,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val patientConfig: Observable<PatientConfig>,
    @Named("exact_date") private val exactDateFormatter: DateTimeFormatter
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
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

    val recentPatientsStream = Observables
        .combineLatest(currentFacilityStream, patientConfig)
        .switchMap { (facility, config) ->
          // Fetching an extra recent patient to know whether we have more than "recentPatientLimit" number of recent patients
          patientRepository.recentPatients(
              facilityUuid = facility.uuid,
              limit = config.recentPatientLimit + 1
          )
        }
        .replay()
        .refCount()

    val updateRecentPatients = recentPatientsStream
        .map { it.map(::recentPatientItem) }
        .withLatestFrom(patientConfig)
        .map { (recentPatients, config) ->
          addSeeAllIfListTooLong(recentPatients, config.recentPatientLimit)
        }
        .map { { ui: Ui -> ui.updateRecentPatients(it) } }

    val toggleEmptyState = recentPatientsStream
        .map { it.isNotEmpty() }
        .map { hasRecentPatients ->
          { ui: Ui -> ui.showOrHideRecentPatients(isVisible = hasRecentPatients) }
        }

    return Observable.merge(updateRecentPatients, toggleEmptyState)
  }

  private fun addSeeAllIfListTooLong(
      recentPatients: List<RecentPatientItem>,
      recentPatientLimit: Int
  ) =
      if (recentPatients.size > recentPatientLimit) {
        recentPatients.take(recentPatientLimit) + SeeAllItem
      } else {
        recentPatients
      }

  private fun recentPatientItem(recentPatient: RecentPatient) =
      RecentPatientItem(
          uuid = recentPatient.uuid,
          name = recentPatient.fullName,
          age = age(recentPatient),
          gender = recentPatient.gender,
          updatedAt = relativeTimestampGenerator.generate(recentPatient.updatedAt, userClock),
          dateFormatter = exactDateFormatter
      )

  private fun age(recentPatient: RecentPatient): Int =
      when (recentPatient.age) {
        null -> estimateCurrentAge(recentPatient.dateOfBirth!!, utcClock)
        else -> {
          val (recordedAge, ageRecordedAtTimestamp) = recentPatient.age
          estimateCurrentAge(recordedAge, ageRecordedAtTimestamp, utcClock)
        }
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
