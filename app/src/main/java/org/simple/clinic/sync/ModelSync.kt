package org.simple.clinic.sync

import io.reactivex.Completable

interface ModelSync {
  val name: String
  val requiresSyncApprovedUser: Boolean

  fun sync(): Completable
  fun push(): Completable
  fun pull(): Completable
  fun syncConfig(): SyncConfig
}
