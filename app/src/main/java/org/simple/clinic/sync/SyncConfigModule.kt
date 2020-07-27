package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import javax.inject.Named

@Module
class SyncConfigModule {

  @Provides
  @Named("sync_config_frequent")
  fun frequentSyncConfig(syncModuleConfig: SyncModuleConfig): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = syncModuleConfig.frequentSyncBatchSize,
        syncGroup = SyncGroup.FREQUENT
    )
  }

  @Provides
  @Named("sync_config_daily")
  fun dailySyncConfig(syncModuleConfig: SyncModuleConfig): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.DAILY,
        batchSize = syncModuleConfig.dailySyncBatchSize,
        syncGroup = SyncGroup.DAILY
    )
  }

  @Provides
  fun syncModuleConfig(reader: ConfigReader): SyncModuleConfig {
    return SyncModuleConfig.read(reader)
  }
}
