package org.simple.clinic.medicalhistory

import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class MedicalHistoryRepository @Inject constructor(
    private val dao: MedicalHistory.RoomDao,
    private val utcClock: UtcClock
) : SynceableRepository<MedicalHistory, MedicalHistoryPayload> {

  fun historyForPatientOrDefault(
      defaultHistoryUuid: UUID,
      patientUuid: PatientUuid
  ): Observable<MedicalHistory> {
    // TODO (vs) 01/06/20: This should not be done here and should be left to the caller. Move later.
    val defaultValue = MedicalHistory(
        uuid = defaultHistoryUuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = Unanswered,
        hasHadHeartAttack = Unanswered,
        hasHadStroke = Unanswered,
        hasHadKidneyDisease = Unanswered,
        diagnosedWithDiabetes = Unanswered,
        syncStatus = SyncStatus.DONE,
        createdAt = Instant.now(utcClock),
        updatedAt = Instant.now(utcClock),
        deletedAt = null)

    return dao.historyForPatient(patientUuid)
        .toObservable()
        .map { histories ->
          if (histories.size > 1) {
            throw AssertionError("DAO shouldn't have returned multiple histories for the same patient")
          }
          if (histories.isEmpty()) {
            // This patient's MedicalHistory hasn't synced yet. We're okay with overriding
            // the values with an empty history instead of say, not showing the medical
            // history at all in patient summary.
            defaultValue

          } else {
            histories.first()
          }
        }
  }

  fun historyForPatientOrDefaultImmediate(
      defaultHistoryUuid: UUID,
      patientUuid: PatientUuid
  ): MedicalHistory {
    // TODO (vs) 01/06/20: This should not be done here and should be left to the caller. Move later.
    val defaultValue = MedicalHistory(
        uuid = defaultHistoryUuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = Unanswered,
        hasHadHeartAttack = Unanswered,
        hasHadStroke = Unanswered,
        hasHadKidneyDisease = Unanswered,
        diagnosedWithDiabetes = Unanswered,
        syncStatus = SyncStatus.DONE,
        createdAt = Instant.now(utcClock),
        updatedAt = Instant.now(utcClock),
        deletedAt = null)

    return dao.historyForPatientImmediate(patientUuid) ?: defaultValue
  }

  fun save(
      uuid: UUID,
      patientUuid: UUID,
      historyEntry: OngoingMedicalHistoryEntry
  ): Completable {
    val medicalHistory = MedicalHistory(
        uuid = uuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = historyEntry.diagnosedWithHypertension,
        hasHadHeartAttack = historyEntry.hasHadHeartAttack,
        hasHadStroke = historyEntry.hasHadStroke,
        hasHadKidneyDisease = historyEntry.hasHadKidneyDisease,
        diagnosedWithDiabetes = historyEntry.hasDiabetes,
        syncStatus = SyncStatus.PENDING,
        createdAt = Instant.now(utcClock),
        updatedAt = Instant.now(utcClock),
        deletedAt = null)
    return save(listOf(medicalHistory))
  }

  fun save(history: MedicalHistory, updateTime: Instant) {
    val dirtyHistory = history.copy(
        syncStatus = SyncStatus.PENDING,
        updatedAt = updateTime)
    dao.save(dirtyHistory)
  }

  override fun save(records: List<MedicalHistory>): Completable {
    return Completable.fromAction {
      dao.save(records)
    }
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): List<MedicalHistory> {
    return dao.recordsWithSyncStatus(syncStatus)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    dao.updateSyncStatus(from, to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError()
    }

    dao.updateSyncStatus(ids, to)
  }

  override fun mergeWithLocalData(payloads: List<MedicalHistoryPayload>) {
    val dirtyRecords = dao.recordIdsWithSyncStatus(SyncStatus.PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { toDatabaseModel(it, SyncStatus.DONE) }

    dao.save(payloadsToSave)
  }

  override fun recordCount(): Observable<Int> {
    return dao.count().toObservable()
  }

  private fun toDatabaseModel(payload: MedicalHistoryPayload, syncStatus: SyncStatus): MedicalHistory {
    return payload.run {
      MedicalHistory(
          uuid = uuid,
          patientUuid = patientUuid,
          // TODO(vs): 2020-01-30 Remove the fallback value when the server changes are available in PROD
          diagnosedWithHypertension = hasHypertension ?: Unanswered,
          hasHadHeartAttack = hasHadHeartAttack,
          hasHadStroke = hasHadStroke,
          hasHadKidneyDisease = hasHadKidneyDisease,
          diagnosedWithDiabetes = hasDiabetes,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt)
    }
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return dao
        .count(SyncStatus.PENDING)
        .toObservable()
  }
}
