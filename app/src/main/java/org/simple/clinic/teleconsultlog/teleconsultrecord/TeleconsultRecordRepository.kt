package org.simple.clinic.teleconsultlog.teleconsultrecord

import io.reactivex.Observable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordRepository @Inject constructor(
    private val teleconsultRecordDao: TeleconsultRecord.RoomDao,
    private val utcClock: UtcClock
) : SynceableRepository<TeleconsultRecord, TeleconsultRecordPayload> {

  fun getTeleconsultRecord(teleconsultRecordId: UUID): TeleconsultRecord? {
    return teleconsultRecordDao.getCompleteTeleconsultLog(teleconsultRecordId)
  }

  fun getPatientTeleconsultRecord(patientUuid: UUID): TeleconsultRecord? {
    return teleconsultRecordDao.latestTeleconsultRecord(patientUuid)
  }

  fun createTeleconsultRecordForMedicalOfficer(
      teleconsultRecordId: UUID,
      patientUuid: UUID,
      medicalOfficerId: UUID,
      teleconsultRecordInfo: TeleconsultRecordInfo
  ) {
    val teleconsultRecord = TeleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = null,
        teleconsultRecordInfo = teleconsultRecordInfo,
        timestamp = Timestamps.create(utcClock),
        syncStatus = SyncStatus.PENDING
    )

    teleconsultRecordDao.save(listOf(teleconsultRecord))
  }

  fun createTeleconsultRequestForNurse(
      teleconsultRecordId: UUID,
      patientUuid: UUID,
      medicalOfficerId: UUID,
      teleconsultRequestInfo: TeleconsultRequestInfo
  ) {
    val teleconsultRecord = TeleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfo,
        teleconsultRecordInfo = null,
        timestamp = Timestamps.create(utcClock),
        syncStatus = SyncStatus.PENDING
    )

    teleconsultRecordDao.save(listOf(teleconsultRecord))
  }

  fun updateMedicalRegistrationId(teleconsultRecordId: UUID, medicalOfficerNumber: String) {
    teleconsultRecordDao.updateMedicalRegistrationId(
        teleconsultRecordId = teleconsultRecordId,
        medicalOfficerNumber = medicalOfficerNumber,
        updatedAt = Instant.now(utcClock),
        syncStatus = SyncStatus.PENDING
    )
  }

  fun updateRequesterCompletionStatus(
      teleconsultRecordId: UUID,
      teleconsultStatus: TeleconsultStatus
  ) {
    teleconsultRecordDao.updateRequesterCompletionStatus(
        teleconsultRecordId = teleconsultRecordId,
        teleconsultStatus = teleconsultStatus,
        updatedAt = Instant.now(utcClock),
        syncStatus = SyncStatus.PENDING
    )
  }

  override fun save(records: List<TeleconsultRecord>) {
    teleconsultRecordDao.save(records)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    teleconsultRecordDao.updateSyncStates(oldStatus = from, newStatus = to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty())
      throw AssertionError()

    teleconsultRecordDao.updateSyncStatus(uuids = ids, newStatus = to)
  }

  override fun mergeWithLocalData(payloads: List<TeleconsultRecordPayload>) {
  }

  override fun recordCount(): Observable<Int> {
    return teleconsultRecordDao.count().toObservable()
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return teleconsultRecordDao
        .countWithStatus(SyncStatus.PENDING)
        .toObservable()
  }

  override fun pendingSyncRecords(limit: Int, offset: Int): List<TeleconsultRecord> {
    return teleconsultRecordDao
        .recordsWithSyncStatusBatched(
            syncStatus = SyncStatus.PENDING,
            limit = limit,
            offset = offset
        )
  }

  fun clear() {
    teleconsultRecordDao.clear()
  }

}
