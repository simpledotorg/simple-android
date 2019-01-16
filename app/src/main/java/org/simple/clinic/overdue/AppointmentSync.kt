package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class AppointmentSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: AppointmentRepository,
    private val apiV1: AppointmentSyncApiV1,
    private val apiV2: AppointmentSyncApiV2,
    private val configProvider: Single<AppointmentConfig>,
    @Named("last_appointment_pull_token") private val lastPullToken: Preference<Optional<String>>,
    private val syncConfigProvider: Single<SyncConfig>
) : ModelSync {

  override fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  override fun push(): Completable {
    return configProvider
        .flatMapCompletable { config ->
          if (config.v2ApiEnabled) {
            syncCoordinator.push(repository) { apiV2.push(toRequest(it)) }
          } else {
            syncCoordinator.push(repository) { apiV1.push(toRequest(it)) }
          }
        }
  }

  override fun pull(): Completable {
    return syncConfigProvider
        .zipWith(configProvider) { syncConfig, appointmentConfig -> syncConfig.batchSize to appointmentConfig.v2ApiEnabled }
        .flatMapCompletable { (batchSize, v2ApiEnabled) ->
          if (v2ApiEnabled) {
            syncCoordinator.pull(repository, lastPullToken, batchSize) { apiV2.pull(batchSize, it) }
          } else {
            syncCoordinator.pull(repository, lastPullToken, batchSize) { apiV1.pull(batchSize, it) }
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
                updatedAt = updatedAt,
                deletedAt = deletedAt)
          }
        }
        .toList()
    return AppointmentPushRequest(payloads)
  }
}
