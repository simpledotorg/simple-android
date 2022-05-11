package org.simple.clinic.overdue.callresult

import org.simple.clinic.patient.SyncStatus
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
    // Not necessary since this is not a resource which we current sync *to* the device
  }

  override fun recordCount() = callResultDao.recordCount()

  override fun pendingSyncRecordCount() = callResultDao.countWithStatus(SyncStatus.PENDING)

  override fun pendingSyncRecords(limit: Int, offset: Int) = callResultDao.recordsWithSyncStatusBatched(
      syncStatus = SyncStatus.PENDING,
      limit = limit,
      offset = offset
  )

  fun callResultForAppointment(appointmentID: UUID): Optional<CallResult> = callResultDao.callResultForAppointment(appointmentID)
}
