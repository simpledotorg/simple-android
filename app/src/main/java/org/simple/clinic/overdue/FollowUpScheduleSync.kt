package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class FollowUpScheduleSync @Inject constructor(
    private val dataSync: DataSync,
    private val repository: FollowUpScheduleRepository,
    private val api: FollowUpScheduleSyncApiV1,
    @Named("last_followupschedule_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
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

  private fun toRequest(schedules: List<FollowUpSchedule>): FollowUpSchedulePushRequest {
    val payloads = schedules
        .map {
          it.run {
            FollowUpSchedulePayload(
                id = id,
                patientId = patientId,
                facilityId = facilityId,
                nextVisit = nextVisit,
                userAction = userAction,
                actionByUserId = actionByUserId,
                reasonForAction = reasonForAction,
                createdAt = createdAt,
                updatedAt = updatedAt)
          }
        }
        .toList()
    return FollowUpSchedulePushRequest(payloads)
  }
}
