package org.simple.clinic.patient.shortcode

import java.util.UUID

class UuidShortCodeCreator(
    val requiredShortCodeLength: Int,
    val characterFilter: CharacterFilter
) {
  init {
    if (requiredShortCodeLength <= 0) {
      throw IllegalArgumentException("Short code length must be > 0!")
    }
  }

  fun createFromUuid(uuid: UUID): UuidShortCode {
    val shortCode = uuid.toString()
        .filter { characterFilter.filter(it) }
        .take(requiredShortCodeLength)

    return if (shortCode.length < requiredShortCodeLength) {
      UuidShortCode.IncompleteShortCode(uuid, shortCode, requiredShortCodeLength)
    } else {
      UuidShortCode.CompleteShortCode(uuid, shortCode)
    }
  }

  interface CharacterFilter {
    fun filter(char: Char): Boolean
  }
}
