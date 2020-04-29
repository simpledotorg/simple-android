package org.simple.clinic.sync

data class SyncConfig(
    val syncInterval: SyncInterval,
    val batchSize: BatchSize,
    val syncGroup: SyncGroup
)
