package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import java.util.Locale
import javax.inject.Named

@Module
class SyncConfigModule {

  @Provides
  @Named("sync_config_frequent")
  fun frequentSyncConfig(syncModuleConfig: SyncModuleConfig): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = syncModuleConfig.frequentSyncBatchSize,
        syncTag = SyncTag.FREQUENT
    )
  }

  @Provides
  @Named("sync_config_daily")
  fun dailySyncConfig(syncModuleConfig: SyncModuleConfig): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.DAILY,
        batchSize = syncModuleConfig.dailySyncBatchSize,
        syncTag = SyncTag.DAILY
    )
  }

  @Provides
  fun syncModuleConfig(reader: ConfigReader): SyncModuleConfig {
    return SyncModuleConfig.read(reader)
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
