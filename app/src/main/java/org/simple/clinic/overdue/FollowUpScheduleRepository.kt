package org.simple.clinic.overdue

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

class FollowUpScheduleRepository @Inject constructor(val dao: FollowUpSchedule.RoomDao) : SynceableRepository<FollowUpSchedule, FollowUpSchedulePayload> {

  fun save(vararg schedules: FollowUpSchedule): Completable {
    return Completable.fromAction {
      dao.save(schedules.toList())
    }
  }

  override fun pendingSyncRecords(): Single<List<FollowUpSchedule>> {
    return dao.withSyncStatus(SyncStatus.PENDING).firstOrError()
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable {
    return Completable.fromAction { dao.updateSyncStatus(from, to) }
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable {
    if (ids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction { dao.updateSyncStatus(ids, to) }
  }

  override fun mergeWithLocalData(payloads: List<FollowUpSchedulePayload>): Completable {
    val newOrUpdatedSchedules = payloads
        .filter { payload: FollowUpSchedulePayload ->
          val localCopy = dao.getOne(payload.id)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { toDatabaseModel(it, SyncStatus.DONE) }
        .toList()

    return Completable.fromAction { dao.save(newOrUpdatedSchedules) }
  }

  private fun toDatabaseModel(payload: FollowUpSchedulePayload, syncStatus: SyncStatus): FollowUpSchedule {
    return payload.run {
      FollowUpSchedule(
          id = id,
          facilityId = facilityId,
          nextVisit = nextVisit,
          userAction = userAction,
          patientId = patientId,
          actionByUserId = actionByUserId,
          reasonForAction = reasonForAction,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt)
    }
  }
}
