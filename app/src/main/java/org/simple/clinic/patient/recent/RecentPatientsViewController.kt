package org.simple.clinic.patient.recent

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.RelativeTimestampGenerator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RecentPatientsView
typealias UiChange = (Ui) -> Unit

class RecentPatientsViewController @Inject constructor(
    private val userSession: UserSession,
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val relativeTimestampGenerator: RelativeTimestampGenerator,
    private val utcClock: UtcClock,
    private val patientConfig: Observable<PatientConfig>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(showRecentPatients(replayedEvents))
  }

  private fun showRecentPatients(events: Observable<UiEvent>): Observable<UiChange> =
      events.ofType<ScreenCreated>()
          .flatMap { userSession.requireLoggedInUser() }
          .map(facilityRepository::currentFacilityUuid)
          .withLatestFrom(patientConfig)
          .flatMap { (facilityUuid, config) ->
            if (facilityUuid != null) {
              patientRepository.recentPatients(facilityUuid, limit = config.recentPatientLimit)
            } else {
              Observable.just(emptyList())
            }
          }
          .map { recentPatients ->
            val recentPatientItems = recentPatients.map {
              RecentPatientItem(
                  name = it.fullName,
                  age = getAge(it),
                  lastBp = it.lastBp?.run {
                    RecentPatientItem.LastBp(
                        systolic = systolic,
                        diastolic = diastolic,
                        updatedAtRelativeTimestamp = relativeTimestampGenerator.generate(updatedAt)
                    )
                  },
                  gender = it.gender
              )
            }
            return@map { ui: Ui ->
              ui.updateRecentPatients(recentPatientItems)
              ui.showNoRecentPatients(isVisible = recentPatientItems.isEmpty())
            }
          }

  private fun getAge(recentPatient: RecentPatient): Int =
      when (recentPatient.age) {
        null -> estimateCurrentAge(recentPatient.dateOfBirth!!, utcClock)
        else -> {
          val (recordedAge, ageRecordedAtTimestamp, _) = recentPatient.age
          estimateCurrentAge(recordedAge, ageRecordedAtTimestamp, utcClock)
        }
      }
}
