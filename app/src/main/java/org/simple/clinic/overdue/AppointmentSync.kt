package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class AppointmentSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: AppointmentRepository,
    private val apiV2: AppointmentSyncApiV2,
    private val apiV3: AppointmentSyncApiV3,
    private val userSession: UserSession,
    private val appointmentConfig: Single<AppointmentConfig>,
    @Named("last_appointment_pull_token") private val lastPullTokenV2: Preference<Optional<String>>,
    @Named("last_appointment_pull_token_v3") private val lastPullTokenV3: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val configProvider: Single<SyncConfig>
) : ModelSync {

  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override fun sync(): Completable =
      canSyncData()
          .flatMapCompletable { canSync ->
            if (canSync) {
              Completable.mergeArrayDelayError(push(), pull())

            } else {
              Completable.complete()
            }
          }

  override fun push(): Completable {
    return appointmentConfig
        .map { it.isApiV3Enabled }
        .flatMapCompletable { isApiV3Enabled ->
          if (isApiV3Enabled) {
            syncCoordinator.push(repository) { apiV3.push(toRequest(it)) }
          } else {
            syncCoordinator.push(repository) { apiV2.push(toRequest(it)) }
          }
        }
  }

  override fun pull(): Completable {
    return Observables
        .combineLatest(configProvider.toObservable(), appointmentConfig.toObservable()) { syncConfig, appointmentConfig ->
          syncConfig.batchSize to appointmentConfig.isApiV3Enabled
        }
        .flatMapCompletable { (batchSize, isApiV3Enabled) ->
          if (isApiV3Enabled) {
            syncCoordinator.pull(repository, lastPullTokenV3, batchSize) { apiV3.pull(batchSize.numberOfRecords, it) }
          } else {
            syncCoordinator.pull(repository, lastPullTokenV2, batchSize) { apiV2.pull(batchSize.numberOfRecords, it) }
          }
        }
  }

  override fun syncConfig() = configProvider

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
                appointmentType = appointmentType,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt)
          }
        }
        .toList()
    return AppointmentPushRequest(payloads)
  }
}
