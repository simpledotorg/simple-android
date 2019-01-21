package org.simple.clinic.sync

data class SyncConfig(
    val syncInterval: SyncInterval,
    val batchSize: BatchSize,
    val syncGroupId: String
) {
  val frequency = syncInterval.frequency

  val backOffDelay = syncInterval.backOffDelay
}
