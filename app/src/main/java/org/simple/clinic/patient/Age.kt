package org.simple.clinic.patient

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

data class Age(
    val value: Int,
    val updatedAt: Instant,
    @Deprecated(message = "This property is no longer in use")
    val computedDateOfBirth: LocalDate
)
