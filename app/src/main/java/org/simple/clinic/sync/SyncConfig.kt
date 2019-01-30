package org.simple.clinic.sync

data class SyncConfig(
    val syncInterval: SyncInterval,
    val batchSizeEnum: BatchSize,
    val syncGroupId: String
) {
  val batchSize = batchSizeEnum.numberOfRecords

  val frequency = syncInterval.frequency

  val backOffDelay = syncInterval.backOffDelay
}
