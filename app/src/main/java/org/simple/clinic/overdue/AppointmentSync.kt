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
        .toList()
    return AppointmentPushRequest(payloads)
  }
}
