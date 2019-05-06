package org.simple.clinic.medicalhistory

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.UNKNOWN
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class MedicalHistoryRepository @Inject constructor(
    private val dao: MedicalHistory.RoomDao,
    private val utcClock: UtcClock
) : SynceableRepository<MedicalHistory, MedicalHistoryPayload> {

  fun historyForPatientOrDefault(patientUuid: PatientUuid): Observable<MedicalHistory> {
    val defaultValue = MedicalHistory(
        uuid = UUID.randomUUID(),
        patientUuid = patientUuid,
        diagnosedWithHypertension = UNKNOWN,
        isOnTreatmentForHypertension = UNKNOWN,
        hasHadHeartAttack = UNKNOWN,
        hasHadStroke = UNKNOWN,
        hasHadKidneyDisease = UNKNOWN,
        hasDiabetes = UNKNOWN,
        syncStatus = SyncStatus.DONE,
        createdAt = Instant.now(utcClock),
        updatedAt = Instant.now(utcClock),
        deletedAt = null,
        recordedAt = Instant.now(utcClock))

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

  fun save(patientUuid: UUID, historyEntry: OngoingMedicalHistoryEntry): Completable {
    val medicalHistory = MedicalHistory(
        uuid = UUID.randomUUID(),
        patientUuid = patientUuid,
        diagnosedWithHypertension = historyEntry.diagnosedWithHypertension,
        isOnTreatmentForHypertension = historyEntry.isOnTreatmentForHypertension,
        hasHadHeartAttack = historyEntry.hasHadHeartAttack,
        hasHadStroke = historyEntry.hasHadStroke,
        hasHadKidneyDisease = historyEntry.hasHadKidneyDisease,
        hasDiabetes = historyEntry.hasDiabetes,
        syncStatus = SyncStatus.PENDING,
        createdAt = Instant.now(utcClock),
        updatedAt = Instant.now(utcClock),
        deletedAt = null,
        recordedAt = Instant.now(utcClock))
    return save(listOf(medicalHistory))
  }

  fun save(history: MedicalHistory, updateTime: () -> Instant = { Instant.now(utcClock) }): Completable {
    return Completable.fromAction {
      val dirtyHistory = history.copy(
          syncStatus = SyncStatus.PENDING,
          updatedAt = updateTime())
      dao.save(dirtyHistory)
    }
  }

  override fun save(records: List<MedicalHistory>): Completable {
    return Completable.fromAction {
      dao.save(records)
    }
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): Single<List<MedicalHistory>> {
    return dao.recordsWithSyncStatus(syncStatus).firstOrError()
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

  override fun mergeWithLocalData(payloads: List<MedicalHistoryPayload>): Completable {
    val newOrUpdatedHistories = payloads
        .filter { payload: MedicalHistoryPayload ->
          val localCopy = dao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { toDatabaseModel(it, SyncStatus.DONE) }
        .toList()

    return Completable.fromAction { dao.save(newOrUpdatedHistories) }
  }

  override fun recordCount(): Observable<Int> {
    return dao.count().toObservable()
  }

  private fun toDatabaseModel(payload: MedicalHistoryPayload, syncStatus: SyncStatus): MedicalHistory {
    return payload.run {
      MedicalHistory(
          uuid = uuid,
          patientUuid = patientUuid,
          diagnosedWithHypertension = diagnosedWithHypertension,
          isOnTreatmentForHypertension = isOnTreatmentForHypertension,
          hasHadHeartAttack = hasHadHeartAttack,
          hasHadStroke = hasHadStroke,
          hasHadKidneyDisease = hasHadKidneyDisease,
          hasDiabetes = hasDiabetes,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt,

          // recordedAt should be changed here when BloodPressureMeasurement payload is updated
          recordedAt = createdAt)
    }
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return dao
        .count(SyncStatus.PENDING)
        .toObservable()
  }
}
