package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject
import javax.inject.Named

class AppointmentSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: AppointmentRepository,
    private val api: AppointmentSyncApi,
    @Named("last_appointment_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "Appointment"

  override val requiresSyncApprovedUser = true

  override fun push() {
    syncCoordinator.push(repository, config.pushBatchSize) { api.push(toRequest(it)).execute().read()!! }
  }

  override fun pull() {
    val batchSize = config.pullBatchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

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
