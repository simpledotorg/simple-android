package org.simple.clinic.util.identifierdisplay

import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.shortcode.UuidShortCode
import org.simple.clinic.patient.shortcode.UuidShortCodeCreator
import java.util.UUID
import javax.inject.Inject

class BpPassportTextConverter @Inject constructor(
    private val uuidShortCodeCreator: UuidShortCodeCreator
) : IdentifierDisplayAdapter.IdentifierToTextConverter {

  override fun convert(identifier: Identifier): CharSequence {
    val uuidShortCode = uuidShortCodeCreator.createFromUuid(UUID.fromString(identifier.value))

    return when (uuidShortCode) {
      is UuidShortCode.CompleteShortCode -> {
        // This is guaranteed to be exactly seven characters in length.
        val prefix = uuidShortCode.shortCode.substring(0, 3)
        val suffix = uuidShortCode.shortCode.substring(3)

        return "$prefix $suffix"
      }
      is UuidShortCode.IncompleteShortCode -> uuidShortCode.shortCode
    }
  }
}
