package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.sync.SyncConfigType.Type.Drugs
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
        syncGroup = SyncGroup.FREQUENT
    )
  }

  /**
   * Drug sync doesn't support batch size at API level yet. So using the normal batch size can cause
   * API loop if the payloads size is greater than the batch size. So, to avoid that issue and also
   * support batching in case server implements batching support for this API in future, we will have
   * a separate `SyncConfig` for the `DrugSync`.
   *
   * We can remove this once medications API has added the batching support and replace this usage
   * with our default sync configs.
   */
  @Provides
  @SyncConfigType(Drugs)
  fun drugsSyncConfig(
      reader: ConfigReader,
      syncInterval: SyncInterval
  ): SyncConfig {
    val drugsBatchSize = reader.long("syncmodule_drugsync_batchsize", default = 1000)

    return SyncConfig(
        syncInterval = syncInterval,
        pullBatchSize = drugsBatchSize.toInt(),
        pushBatchSize = 0, // We don't push drugs to server, so this is unused
        syncGroup = SyncGroup.FREQUENT
    )
  }
}
