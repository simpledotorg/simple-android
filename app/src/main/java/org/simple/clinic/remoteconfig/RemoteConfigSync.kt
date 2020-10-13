package org.simple.clinic.remoteconfig

import io.reactivex.Completable
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncTag
import org.simple.clinic.sync.SyncInterval
import javax.inject.Inject

class RemoteConfigSync @Inject constructor(
    private val remoteConfigService: RemoteConfigService
) : ModelSync {

  override val name: String = "Remote Config"

  override val requiresSyncApprovedUser = false

  override fun sync(): Completable = Completable.fromAction { pull() }

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull() {
    remoteConfigService.update()
  }

  override fun syncConfig(): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = 0, // Unused for remote config sync
        syncTag = SyncTag.FREQUENT
    )
  }
}
