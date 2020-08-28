package org.simple.clinic.sync

import io.reactivex.Completable

interface ModelSync {
  val name: String
  val requiresSyncApprovedUser: Boolean

  @Deprecated(
      message = "Use push() or pull() directly based on your needs",
      replaceWith = ReplaceWith(
          expression = "Completable.mergeArrayDelayError(Completable.fromAction { push() }, Completable.fromAction { pull() })",
          imports = ["io.reactivex.Completable"]
      )
  )
  fun sync(): Completable
  fun push()
  fun pull()
  fun syncConfig(): SyncConfig
}
