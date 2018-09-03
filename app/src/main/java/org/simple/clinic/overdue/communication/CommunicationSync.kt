package org.simple.clinic.overdue.communication

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class CommunicationSync @Inject constructor(
    private val dataSync: DataSync,
    private val repository: CommunicationRepository,
    private val api: CommunicationSyncApiV1,
    @Named("last_communication_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
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

  private fun toRequest(schedules: List<Communication>): CommunicationPushRequest {
    val payloads = schedules
        .map {
          it.run {
            CommunicationPayload(
                uuid = uuid,
                appointmentUuid = appointmentUuid,
                userUuid = userUuid,
                type = type,
                result = result,
                createdAt = createdAt,
                updatedAt = updatedAt)
          }
        }
        .toList()
    return CommunicationPushRequest(payloads)
  }
}
