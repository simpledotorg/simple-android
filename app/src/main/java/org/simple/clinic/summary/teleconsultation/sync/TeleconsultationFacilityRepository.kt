package org.simple.clinic.summary.teleconsultation.sync

import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.AppDatabase
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

class TeleconsultationFacilityRepository @Inject constructor(
    private val appDatabase: AppDatabase
) : SynceableRepository<TeleconsultationFacilityWithMedicalOfficers, TeleconsultationFacilityInfoPayload> {

  override fun save(records: List<TeleconsultationFacilityWithMedicalOfficers>): Completable {
    return Completable.fromAction { saveRecords(records) }
  }

  private fun saveRecords(records: List<TeleconsultationFacilityWithMedicalOfficers>) {
    saveTeleconsultFacilityInfo(records)
    saveTeleconsultMedicalOfficers(records)
    saveTeleconsultMedicalOfficersCrossRef(records)
  }

  private fun saveTeleconsultMedicalOfficersCrossRef(records: List<TeleconsultationFacilityWithMedicalOfficers>) {
    appDatabase
        .teleconsultFacilityWithMedicalOfficersDao()
        .save(records.flatMap { (teleconsultInfo, medicalOfficers) ->
          medicalOfficers.map {
            TeleconsultationFacilityMedicalOfficersCrossRef(
                teleconsultInfo.facilityId,
                it.medicalOfficerId
            )
          }
        })
  }

  private fun saveTeleconsultMedicalOfficers(records: List<TeleconsultationFacilityWithMedicalOfficers>) {
    appDatabase
        .teleconsultMedicalOfficersDao()
        .save(records.flatMap { it.medicalOfficers })
  }

  private fun saveTeleconsultFacilityInfo(records: List<TeleconsultationFacilityWithMedicalOfficers>) {
    appDatabase
        .teleconsultFacilityInfoDao()
        .save(records.map { it.teleconsultationFacilityInfo })
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): List<TeleconsultationFacilityWithMedicalOfficers> {
    return appDatabase
        .teleconsultFacilityInfoDao()
        .recordsWithSyncStatus(syncStatus)
        .map {
          appDatabase
              .teleconsultFacilityWithMedicalOfficersDao()
              .getOne(it.facilityId)!!
        }
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    appDatabase.teleconsultFacilityInfoDao().updateSyncStatus(from, to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    appDatabase.teleconsultFacilityInfoDao().updateSyncStatusForIds(ids, to)
  }

  override fun mergeWithLocalData(payloads: List<TeleconsultationFacilityInfoPayload>) {
    val dirtyRecords = appDatabase.teleconsultFacilityInfoDao().recordIdsWithSyncStatus(PENDING)

    val payloadsToSave = payloads
        .filterNot { it.id in dirtyRecords }
        .map { it.toTeleconsultInfoWithMedicalOfficersDatabaseModel() }

    saveRecords(payloadsToSave)
  }

  override fun recordCount(): Observable<Int> {
    return Observable.fromCallable {
      appDatabase
          .teleconsultFacilityInfoDao()
          .count()
    }
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return Observable.fromCallable {
      appDatabase
          .teleconsultFacilityInfoDao()
          .countWithStatus(PENDING)
    }
  }

  fun medicalOfficersForFacility(facilityId: UUID): List<MedicalOfficer> {
    return appDatabase
        .teleconsultFacilityWithMedicalOfficersDao()
        .getOne(facilityId)
        ?.medicalOfficers
        .orEmpty()
  }

  fun getAll(): List<TeleconsultationFacilityWithMedicalOfficers> {
    return appDatabase
        .teleconsultFacilityWithMedicalOfficersDao()
        .getAll()
  }

  fun clear() {
    appDatabase.teleconsultFacilityInfoDao().clear()
    appDatabase.teleconsultMedicalOfficersDao().clear()
    appDatabase.teleconsultFacilityWithMedicalOfficersDao().clear()
  }
}
