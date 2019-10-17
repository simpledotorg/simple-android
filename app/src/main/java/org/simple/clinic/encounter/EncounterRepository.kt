package org.simple.clinic.encounter

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.encounter.sync.EncounterPayload
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

class EncounterRepository @Inject constructor(
    private val database: AppDatabase
) : SynceableRepository<Encounter, EncounterPayload> {

  override fun save(records: List<Encounter>): Completable {
    return database.encountersDao().save(encounters = records)
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
    TODO("not implemented")
  }

  override fun recordCount(): Observable<Int> {
    return database.encountersDao().recordCount()
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return database.encountersDao().recordCount(syncStatus = PENDING)
  }
}


