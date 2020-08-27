package org.simple.clinic.sync

import io.reactivex.Completable

interface ModelSync {
  val name: String

  fun sync(): Completable
  fun push(): Completable
  fun pull(): Completable
  fun syncConfig(): SyncConfig
}
