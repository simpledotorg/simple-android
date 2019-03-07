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
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
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

  private fun showRecentPatients(events: Observable<UiEvent>): Observable<UiChange> {
    val facilityUuidStream = events.ofType<ScreenCreated>()
        .flatMap { userSession.requireLoggedInUser() }
        .map { Optional.toOptional(facilityRepository.currentFacilityUuid(it)) }
        .replay()
        .refCount()

    val facilityIdNotPresent = facilityUuidStream
        .ofType<None>()
        .map {
          { ui: Ui ->
            ui.clearRecentPatients()
            ui.showNoRecentPatients()
          }
        }

    val recentPatientsStream = facilityUuidStream
        .ofType<Just<UUID>>()
        .map { it.value }
        .withLatestFrom(patientConfig)
        .flatMap { (facilityUuid, config) ->
          patientRepository.recentPatients(facilityUuid, limit = config.recentPatientLimit)
        }
        .replay()
        .refCount()

    val zeroRecentPatients = recentPatientsStream
        .filter { it.isEmpty() }
        .map {
          { ui: Ui ->
            ui.clearRecentPatients()
            ui.showNoRecentPatients()
          }
        }

    val nonZeroRecentPatients = recentPatientsStream
        .filter { it.isNotEmpty() }
        .map { recentPatients ->
          { ui: Ui ->
            ui.updateRecentPatients(recentPatients.map(::recentPatientItem))
            ui.hideNoRecentPatients()
          }
        }
    return Observable.merge(facilityIdNotPresent, zeroRecentPatients, nonZeroRecentPatients)
  }

  private fun recentPatientItem(recentPatient: RecentPatient) =
      RecentPatientItem(
          name = recentPatient.fullName,
          age = getAge(recentPatient),
          lastBp = recentPatient.lastBp?.run {
            RecentPatientItem.LastBp(
                systolic = systolic,
                diastolic = diastolic,
                updatedAtRelativeTimestamp = relativeTimestampGenerator.generate(updatedAt)
            )
          },
          gender = recentPatient.gender
      )

  private fun getAge(recentPatient: RecentPatient): Int =
      when (recentPatient.age) {
        null -> estimateCurrentAge(recentPatient.dateOfBirth!!, utcClock)
        else -> {
          val (recordedAge, ageRecordedAtTimestamp, _) = recentPatient.age
          estimateCurrentAge(recordedAge, ageRecordedAtTimestamp, utcClock)
        }
      }
}
