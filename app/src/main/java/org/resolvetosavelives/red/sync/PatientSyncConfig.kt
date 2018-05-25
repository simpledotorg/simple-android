package org.resolvetosavelives.red.sync

import org.threeten.bp.Duration

// TODO: We should think about limiting the number of records to push/pull.
data class PatientSyncConfig(

    val frequency: Duration
)
