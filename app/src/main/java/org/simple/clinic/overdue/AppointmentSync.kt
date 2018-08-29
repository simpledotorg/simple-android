package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class AppointmentSync @Inject constructor(
    private val dataSync: DataSync,
    private val repository: AppointmentRepository,
    private val api: AppointmentSyncApiV1,
    @Named("last_appointment_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
) {

  fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  fun push(): Completable {
    return dataSync.push(repository, pushNetworkCall = { api.push(toRequest(it)) })
  }

  fun pull(): Completable {
    return dataSync.pull(
        repository = repository,
        lastPullTimestamp = lastPullTimestamp,
        pullNetworkCall = api::pull)
  }

  private fun toRequest(schedules: List<Appointment>): AppointmentPushRequest {
    val payloads = schedules
        .map {
          it.run {
            AppointmentPayload(
                id = id,
                patientId = patientId,
                facilityId = facilityId,
                date = date,
                status = status,
                statusReason = statusReason,
                createdAt = createdAt,
                updatedAt = updatedAt)
          }
        }
        .toList()
    return AppointmentPushRequest(payloads)
  }
}
