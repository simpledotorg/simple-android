package org.simple.clinic.sync

import org.threeten.bp.Duration

data class SyncConfig(

    val frequency: Duration,

    val batchSize: Int // Number of records to sync in per network call
)
