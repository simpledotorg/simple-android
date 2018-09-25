package org.simple.clinic.medicalhistory

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class MedicalHistoryRepository @Inject constructor(
    private val dao: MedicalHistory.RoomDao,
    private val clock: Clock
) : SynceableRepository<MedicalHistory, MedicalHistoryPayload> {

  fun historyForPatient(patientUuid: PatientUuid): Observable<MedicalHistory> {
    return dao.historyForPatient(patientUuid)
        .map { patients ->
          when {
            patients.isNotEmpty() -> patients.first()
            else -> throw AssertionError("Medical History isn't present for $patientUuid")
          }
        }
        .toObservable()
  }

  fun save(patientUuid: UUID, historyEntry: OngoingMedicalHistoryEntry): Completable {
    val medicalHistory = MedicalHistory(
        uuid = UUID.randomUUID(),
        patientUuid = patientUuid,
        hasHadHeartAttack = historyEntry.hasHadHeartAttack,
        hasHadStroke = historyEntry.hasHadStroke,
        hasHadKidneyDisease = historyEntry.hasHadKidneyDisease,
        isOnTreatmentForHypertension = historyEntry.isOnTreatmentForHypertension,
        hasDiabetes = historyEntry.hasDiabetes,
        syncStatus = SyncStatus.PENDING,
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock))
    return save(listOf(medicalHistory))
  }

  fun update(history: MedicalHistory): Completable {
    return Completable.fromAction {
      val dirtyHistory = history.copy(
          syncStatus = SyncStatus.PENDING,
          updatedAt = Instant.now(clock))
      dao.update(dirtyHistory)
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

  override fun recordCount(): Single<Int> {
    return dao.count().firstOrError()
  }

  private fun toDatabaseModel(payload: MedicalHistoryPayload, syncStatus: SyncStatus): MedicalHistory {
    return payload.run {
      MedicalHistory(
          uuid = uuid,
          patientUuid = patientUuid,
          hasHadHeartAttack = hasHadHeartAttack,
          hasHadStroke = hasHadStroke,
          hasHadKidneyDisease = hasHadKidneyDisease,
          isOnTreatmentForHypertension = isOnTreatmentForHypertension,
          hasDiabetes = hasDiabetes,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt)
    }
  }
}
