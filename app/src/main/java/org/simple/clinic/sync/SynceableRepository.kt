package org.simple.clinic.sync

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.patient.SyncStatus
import java.util.UUID

interface SynceableRepository<T, P> {

  fun pendingSyncRecords(): Single<List<T>>

  fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable

  fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable

  fun mergeWithLocalData(payloads: List<P>): Completable
}
