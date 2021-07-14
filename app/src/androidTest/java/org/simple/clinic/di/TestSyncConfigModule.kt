package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval

@Module
class TestSyncConfigModule {

  @Provides
  @SyncConfigType(Frequent)
  fun frequentSyncConfig(): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        pullBatchSize = 1000,
        pushBatchSize = 500,
        syncGroup = SyncGroup.FREQUENT
    )
  }
}
