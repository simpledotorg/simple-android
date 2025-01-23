package org.simple.clinic.cvdrisk

import io.reactivex.Observable
import org.simple.clinic.cvdrisk.sync.CVDRiskPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class CVDRiskRepository @Inject constructor(
    val dao: CVDRisk.RoomDao,
    private val utcClock: UtcClock
) : SynceableRepository<CVDRisk, CVDRiskPayload> {
  fun save(
      riskScore: CVDRiskRange,
      patientUuid: UUID,
      uuid: UUID,
  ) {
    val cvdRisk = CVDRisk(
        uuid = uuid,
        patientUuid = patientUuid,
        riskScore = riskScore,
        timestamps = Timestamps.create(utcClock),
        syncStatus = PENDING
    )
    dao.saveRisk(cvdRisk)
  }

  fun save(cvdRisk: CVDRisk, updateAt: Instant) {
    val updatedCVDRisk = cvdRisk.copy(
        syncStatus = PENDING,
        timestamps = cvdRisk.timestamps.copy(updatedAt = updateAt))
    dao.saveRisk(updatedCVDRisk)
  }

  override fun save(records: List<CVDRisk>) {
    dao.saveRisks(records)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    dao.updateSyncStatus(from, to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError()
    }

    dao.updateSyncStatusForIds(ids, to)
  }

  override fun recordCount(): Observable<Int> = dao.count().toObservable()

  override fun pendingSyncRecordCount(): Observable<Int> =
      dao.countWithStatus(PENDING).toObservable()

  override fun pendingSyncRecords(limit: Int, offset: Int): List<CVDRisk> {
    return dao
        .recordsWithSyncStatusBatched(
            syncStatus = PENDING,
            limit = limit,
            offset = offset
        )
  }

  override fun mergeWithLocalData(payloads: List<CVDRiskPayload>) {
    val dirtyRecords = dao.recordIdsWithSyncStatus(PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { it.toDatabaseModel(SyncStatus.DONE) }

    dao.saveRisks(payloadsToSave)
  }

  fun getCVDRiskImmediate(patientUuid: UUID): CVDRisk? {
    return dao.cvdRiskImmediate(patientUuid)
  }

  fun recordsWithSyncStatus(syncStatus: SyncStatus): List<CVDRisk> {
    return dao.recordsWithSyncStatus(syncStatus)
  }
}
