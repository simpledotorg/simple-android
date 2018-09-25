package org.simple.clinic.patient

import org.threeten.bp.Instant
import java.util.UUID

data class LastBp(
    val takenOn: Instant,
    val takenAtFacilityName: String,
    val takenAtFacilityUuid: UUID
)
