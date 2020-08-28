package org.simple.clinic.sync

import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.patient.SyncStatus
import java.util.UUID

/**
 * @param T Type of local data model.
 * @param P Type of payload for the local model.
 */
interface SynceableRepository<T, P> {

  fun save(records: List<T>): Completable

  fun recordsWithSyncStatus(syncStatus: SyncStatus): List<T>

  fun setSyncStatus(from: SyncStatus, to: SyncStatus)

  fun setSyncStatus(ids: List<UUID>, to: SyncStatus)

  fun mergeWithLocalData(payloads: List<P>)

  fun recordCount(): Observable<Int>

  fun pendingSyncRecordCount(): Observable<Int>
}
