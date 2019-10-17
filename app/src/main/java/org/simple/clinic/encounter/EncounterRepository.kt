package org.simple.clinic.encounter

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.encounter.sync.EncounterPayload
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

class EncounterRepository @Inject constructor(
    private val database: AppDatabase
) : SynceableRepository<Encounter, EncounterPayload> {

  override fun save(records: List<Encounter>): Completable {
    return Completable.fromAction { database.encountersDao().save(encounters = records) }
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): Single<List<Encounter>> {
    return database.encountersDao().recordsWithSyncStatus(syncStatus).firstOrError()
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable {
    return database.encountersDao().updateSyncStatus(from, to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable {
    return database.encountersDao().updateSyncStatus(ids, to)
  }

  override fun mergeWithLocalData(payloads: List<EncounterPayload>): Completable {
    return Single.fromCallable {
      payloads
          .filter { payload ->
            database.encountersDao()
                .getOne(payload.uuid)
                ?.syncStatus.canBeOverriddenByServerCopy()
          }
          .map(::payloadToEncounters)
    }.flatMapCompletable(::saveMergedEncounters)
  }

  private fun payloadToEncounters(payload: EncounterPayload): EncounterAndObservations {
    val bloodPressures = payload.observations.bloodPressureMeasurements.map { bps ->
      bps.toDatabaseModel(syncStatus = DONE, encounterUuid = payload.uuid)
    }
    return EncounterAndObservations(encounter = payload.toDatabaseModel(DONE), bloodPressures = bloodPressures)
  }

  private fun saveMergedEncounters(records: List<EncounterAndObservations>): Completable {
    return Completable.fromAction {
      database
          .bloodPressureDao()
          .save(records.flatMap { it.bloodPressures })

      database.encountersDao().save(records.map { it.encounter })
    }
  }

  override fun recordCount(): Observable<Int> {
    return database.encountersDao().recordCount()
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return database.encountersDao().recordCount(syncStatus = PENDING)
  }
}


