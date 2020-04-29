package org.simple.clinic.sync

import io.reactivex.Completable
import io.reactivex.Single

interface ModelSync {
  fun sync(): Completable
  fun push(): Completable
  fun pull(): Completable
  fun syncConfig(): Single<SyncConfig>
}
