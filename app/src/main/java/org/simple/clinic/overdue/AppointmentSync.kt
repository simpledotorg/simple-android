package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class AppointmentSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: AppointmentRepository,
    private val apiV1: AppointmentSyncApiV1,
    private val apiV2: AppointmentSyncApiV2,
    private val configProvider: Single<AppointmentConfig>,
    @Named("last_appointment_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
) {

  fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  fun push(): Completable {
    return configProvider
        .flatMapCompletable { config ->
          if (config.v2ApiEnabled) {
            syncCoordinator.push(repository) { apiV2.push(toRequest(it)) }
          } else {
            syncCoordinator.push(repository) { apiV1.push(toRequest(it)) }
          }
        }
  }

  fun pull(): Completable {
    return configProvider
        .flatMapCompletable { config ->
          if (config.v2ApiEnabled) {
            syncCoordinator.pull(repository, lastPullTimestamp, apiV2::pull)
          } else {
            syncCoordinator.pull(repository, lastPullTimestamp, apiV1::pull)
          }
        }
  }

  private fun toRequest(appointments: List<Appointment>): AppointmentPushRequest {
    val payloads = appointments
        .map {
          it.run {
            AppointmentPayload(
                uuid = uuid,
                patientUuid = patientUuid,
                facilityUuid = facilityUuid,
                date = scheduledDate,
                status = status,
                cancelReason = cancelReason,
                remindOn = remindOn,
                agreedToVisit = agreedToVisit,
                createdAt = createdAt,
                updatedAt = updatedAt)
          }
        }
    return AppointmentPushRequest(payloads)
  }
}
