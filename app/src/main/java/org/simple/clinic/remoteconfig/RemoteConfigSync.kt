package org.simple.clinic.remoteconfig

import io.reactivex.Completable
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import javax.inject.Inject

class RemoteConfigSync @Inject constructor(
    private val crashReporter: CrashReporter,
    private val remoteConfigService: RemoteConfigService
) : ModelSync {

  override val name: String = "Remote Config"

  override val requiresSyncApprovedUser = false

  override fun sync(): Completable = pull()

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull(): Completable {
    return remoteConfigService.update()
  }

  override fun syncConfig(): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = 0, // Unused for remote config sync
        syncGroup = SyncGroup.FREQUENT
    )
  }
}
