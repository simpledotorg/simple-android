package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import javax.inject.Named

@Module
class SyncConfigModule {

  @Provides
  @Named("sync_config_frequent")
  fun frequentSyncConfig(
      reader: ConfigReader
  ): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        pullBatchSize = reader.long("sync_pull_batch_size", 1000).toInt(),
        pushBatchSize = reader.long("sync_push_batch_size", 500).toInt(),
        syncGroup = SyncGroup.FREQUENT
    )
  }

  @Provides
  @Named("sync_config_daily")
  fun dailySyncConfig(
      reader: ConfigReader
  ): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.DAILY,
        pullBatchSize = reader.long("sync_pull_batch_size", 1000).toInt(),
        pushBatchSize = reader.long("sync_push_batch_size", 500).toInt(),
        syncGroup = SyncGroup.DAILY
    )
  }
}
