package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
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
    private val api: AppointmentSyncApi,
    private val userSession: UserSession,
    @Named("last_appointment_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override val name: String = "Appointment"

  override fun sync(): Completable =
      canSyncData()
          .flatMapCompletable { canSync ->
            if (canSync) {
              Completable.mergeArrayDelayError(push(), pull())

            } else {
              Completable.complete()
            }
          }

  override fun push() = Completable.fromAction { syncCoordinator.push(repository) { api.push(toRequest(it)).execute().body()!! } }

  override fun pull(): Completable {
    return Completable.fromAction {
      val batchSize = config.batchSize
      syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().body()!! }
    }
  }

  override fun syncConfig(): SyncConfig = config

  private fun toRequest(appointments: List<Appointment>): AppointmentPushRequest {
    val payloads = appointments
        .map {
          it.run {
            AppointmentPayload(
                uuid = uuid,
                patientUuid = patientUuid,
                facilityUuid = facilityUuid,
                creationFacilityUuid = creationFacilityUuid,
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
