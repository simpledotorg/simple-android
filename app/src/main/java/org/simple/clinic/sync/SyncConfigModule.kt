package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.sync.SyncConfigType.Type.Drugs
import java.util.Locale
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
  fun drugsSyncConfig(reader: ConfigReader): SyncConfig {
    val drugsBatchSize = reader.long("syncmodule_drugsync_batchsize", default = 1000)

    return SyncConfig(
        syncInterval = SyncInterval.DAILY,
        pullBatchSize = drugsBatchSize.toInt(),
        pushBatchSize = 0, // We don't push drugs to server, so this is unused
        syncGroup = SyncGroup.DAILY
    )
  }

  data class SyncModuleConfig(
      val frequentSyncBatchSize: Int,
      val dailySyncBatchSize: Int
  ) {

    companion object {

      fun read(reader: ConfigReader): SyncModuleConfig {
        val frequentConfigString = reader.string("syncmodule_frequentsync_batchsize", default = "large")
        val frequentBatchSize = findBatchSizeForKey(frequentConfigString)

        val dailyConfigString = reader.string("syncmodule_dailysync_batchsize", default = "large")
        val dailyBatchSize = findBatchSizeForKey(dailyConfigString)

        return SyncModuleConfig(
            frequentSyncBatchSize = frequentBatchSize,
            dailySyncBatchSize = dailyBatchSize
        )
      }

      private fun findBatchSizeForKey(key: String): Int {
        return when (key.toLowerCase(Locale.ROOT)) {
          "verysmall" -> 10
          "small" -> 150
          "medium" -> 500
          "large" -> 1000
          else -> 500
        }
      }
    }
  }
}
