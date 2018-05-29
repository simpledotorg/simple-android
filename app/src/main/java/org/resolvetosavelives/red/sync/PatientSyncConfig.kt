package org.resolvetosavelives.red.sync

import org.threeten.bp.Duration

// TODO: We should think about limiting the number of records to push/pull.
data class PatientSyncConfig(

    val frequency: Duration,

    /** Number of patients to sync in one network call */
    val batchSize: Int
)
