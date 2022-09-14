package org.simple.clinic.overdue.callresult

import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.sync.SynceableRepository
import java.util.Optional
import java.util.UUID
import javax.inject.Inject

class CallResultRepository @Inject constructor(
    private val callResultDao: CallResult.RoomDao
) : SynceableRepository<CallResult, CallResultPayload> {

  override fun save(records: List<CallResult>) {
    callResultDao.save(records)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    callResultDao.updateSyncStatus(
        oldStatus = from,
        newStatus = to
    )
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    callResultDao.updateSyncStatusForIds(
        callResultIds = ids,
        newStatus = to
    )
  }

  override fun mergeWithLocalData(payloads: List<CallResultPayload>) {
    val pendingRecords = callResultDao.recordIdsWithSyncStatus(PENDING)

    val payloadsToSave = payloads
        .filterNot { it.id in pendingRecords }
        .map { it.toDatabaseModel(DONE) }

    callResultDao.save(payloadsToSave)
  }

  override fun recordCount() = callResultDao.recordCount()

  override fun pendingSyncRecordCount() = callResultDao.countWithStatus(PENDING)

  override fun pendingSyncRecords(limit: Int, offset: Int) = callResultDao.recordsWithSyncStatusBatched(
      syncStatus = PENDING,
      limit = limit,
      offset = offset
  )

  fun callResultForAppointment(appointmentID: UUID): Optional<CallResult> = callResultDao.callResultForAppointment(appointmentID)

  fun recordsWithSyncStatus(syncStatus: SyncStatus): List<CallResult> = callResultDao.recordsWithSyncStatus(syncStatus)
}
