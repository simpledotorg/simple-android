package org.simple.clinic.patient

import org.threeten.bp.Instant

data class LastBp(
    val takenOn: Instant,
    val takenAtFacilityName: String
)
