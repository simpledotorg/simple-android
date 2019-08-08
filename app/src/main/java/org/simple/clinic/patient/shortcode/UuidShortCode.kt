package org.simple.clinic.patient.shortcode

import java.util.UUID

data class UuidShortCode(
    val uuid: UUID,
    val shortCode: String,
    val requiredLength: Int
) {
  val isComplete: Boolean
    get() = shortCode.length == requiredLength
}
