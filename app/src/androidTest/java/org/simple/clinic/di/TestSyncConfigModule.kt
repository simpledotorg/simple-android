package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.sync.BatchSize
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import javax.inject.Named

@Module
class TestSyncConfigModule {

  @Provides
  @Named("sync_config_frequent")
  fun frequentSyncConfig(): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = BatchSize.VERY_SMALL,
        syncGroup = SyncGroup.FREQUENT
    )
  }

  @Provides
  @Named("sync_config_daily")
  fun dailySyncConfig(): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.DAILY,
        batchSize = BatchSize.VERY_SMALL,
        syncGroup = SyncGroup.DAILY
    )
  }
}
