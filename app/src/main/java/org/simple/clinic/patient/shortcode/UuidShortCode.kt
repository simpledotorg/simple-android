package org.simple.clinic.patient.shortcode

import java.util.UUID

sealed class UuidShortCode {

  data class CompleteShortCode(
      val uuid: UUID,
      val shortCode: String
  ): UuidShortCode()

  data class IncompleteShortCode(
      val uuid: UUID,
      val shortCode: String,
      val requiredShortCodeLength: Int
  ): UuidShortCode()
}
