package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import java.time.Duration

@Module
class SyncConfigModule {

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
      reader: ConfigReader,
      syncInterval: SyncInterval
  ): SyncConfig {
    return SyncConfig(
        syncInterval = syncInterval,
        pullBatchSize = reader.long("sync_pull_batch_size", 1000).toInt(),
        pushBatchSize = reader.long("sync_push_batch_size", 500).toInt(),
        syncGroup = SyncGroup.FREQUENT,
        name = "sync-patient-resources"
    )
  }
}
