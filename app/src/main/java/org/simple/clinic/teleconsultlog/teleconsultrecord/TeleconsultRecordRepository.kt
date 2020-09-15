package org.simple.clinic.teleconsultlog.teleconsultrecord

import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.util.UtcClock
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordRepository @Inject constructor(
    private val teleconsultRecordDao: TeleconsultRecord.RoomDao,
    private val teleconsultRecordWithPrescribedDrugsDao: TeleconsultRecordWithPrescribedDrugs.RoomDao,
    private val utcClock: UtcClock,
    private val teleconsultRecordPrescribedDrugDao: TeleconsultRecordPrescribedDrug.RoomDao
) : SynceableRepository<TeleconsultRecord, TeleconsultRecordPayload> {

  fun getTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId: UUID): TeleconsultRecordWithPrescribedDrugs? {
    return teleconsultRecordWithPrescribedDrugsDao.getCompleteTeleconsultLog(teleconsultRecordId)
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

  override fun save(records: List<TeleconsultRecord>): Completable {
    return Completable.fromAction { teleconsultRecordDao.save(records) }
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): List<TeleconsultRecord> {
    return teleconsultRecordDao
        .recordsWithSyncStatus(syncStatus)
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
        .count(SyncStatus.PENDING)
        .toObservable()
  }

  fun saveTeleconsultRecordWithPrescribedDrug(teleconsultRecordPrescribedDrugs: List<TeleconsultRecordPrescribedDrug>) {
    teleconsultRecordPrescribedDrugDao.save(teleconsultRecordPrescribedDrugs)
  }

  fun clear() {
    teleconsultRecordDao.clear()
  }
}
