package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import java.time.Duration

@Module
class TestSyncConfigModule {

  @Provides
  fun syncInterval(): SyncInterval {
    return SyncInterval(
        frequency = Duration.ofMinutes(16),
        backOffDelay = Duration.ofMinutes(5L)
    )
  }

  @Provides
  @SyncConfigType(Frequent)
  fun frequentSyncConfig(
      syncInterval: SyncInterval
  ): SyncConfig {
    return SyncConfig(
        syncInterval = syncInterval,
        pullBatchSize = 1000,
        pushBatchSize = 500,
        syncGroup = SyncGroup.FREQUENT
    )
  }
}
