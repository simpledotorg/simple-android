package org.simple.clinic.teleconsultlog.drugduration

import java.time.Duration
import java.util.UUID

data class SavedDrugDuration(
    val drugUuid: UUID,
    val duration: Duration
)
