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
        batchSize = syncModuleConfig.frequentSyncBatchSize.numberOfRecords,
        syncGroup = SyncGroup.FREQUENT
    )
  }

  @Provides
  @Named("sync_config_daily")
  fun dailySyncConfig(syncModuleConfig: SyncModuleConfig): SyncConfig {
    return SyncConfig(
        syncInterval = SyncInterval.DAILY,
        batchSize = syncModuleConfig.dailySyncBatchSize.numberOfRecords,
        syncGroup = SyncGroup.DAILY
    )
  }

  @Provides
  fun syncModuleConfig(reader: ConfigReader): SyncModuleConfig {
    return SyncModuleConfig.read(reader)
  }

  data class SyncModuleConfig(
      val frequentSyncBatchSize: BatchSize,
      val dailySyncBatchSize: BatchSize
  ) {

    companion object {

      fun read(reader: ConfigReader): SyncModuleConfig {
        val frequentConfigString = reader.string("syncmodule_frequentsync_batchsize", default = "large")

        val frequentBatchSize = when (frequentConfigString.toLowerCase(Locale.ROOT)) {
          "verysmall" -> BatchSize.VERY_SMALL
          "small" -> BatchSize.SMALL
          "medium" -> BatchSize.MEDIUM
          "large" -> BatchSize.LARGE
          else -> BatchSize.MEDIUM
        }

        val dailyConfigString = reader.string("syncmodule_dailysync_batchsize", default = "large")

        val dailyBatchSize = when (dailyConfigString.toLowerCase(Locale.ROOT)) {
          "verysmall" -> BatchSize.VERY_SMALL
          "small" -> BatchSize.SMALL
          "medium" -> BatchSize.MEDIUM
          "large" -> BatchSize.LARGE
          else -> BatchSize.MEDIUM
        }

        return SyncModuleConfig(
            frequentSyncBatchSize = frequentBatchSize,
            dailySyncBatchSize = dailyBatchSize
        )
      }
    }
  }
}
