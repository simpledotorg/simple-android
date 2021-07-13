package org.simple.clinic.sync

data class SyncConfig(
    val syncInterval: SyncInterval,
    val pullBatchSize: Int,
    val pushBatchSize: Int,
    val syncGroup: SyncGroup
)
