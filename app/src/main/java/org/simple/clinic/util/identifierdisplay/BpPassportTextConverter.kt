package org.simple.clinic.util.identifierdisplay

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.shortcode.UuidShortCode
import org.simple.clinic.patient.shortcode.UuidShortCodeCreator
import org.simple.clinic.util.Unicode
import java.util.UUID
import javax.inject.Inject

class BpPassportTextConverter @Inject constructor(
    private val uuidShortCodeCreator: UuidShortCodeCreator,
    private val resources: Resources
) : IdentifierDisplayAdapter.IdentifierToTextConverter {

  override fun convertValue(identifier: Identifier): String {
    val uuidShortCode = uuidShortCodeCreator.createFromUuid(UUID.fromString(identifier.value))

    return when (uuidShortCode) {
      is UuidShortCode.CompleteShortCode -> {
        // This is guaranteed to be exactly seven characters in length.
        val prefix = uuidShortCode.shortCode.substring(0, 3)
        val suffix = uuidShortCode.shortCode.substring(3)

        return "$prefix${Unicode.nonBreakingSpace}$suffix"
      }
      is UuidShortCode.IncompleteShortCode -> uuidShortCode.shortCode
    }
  }

  override fun convertType(identifier: Identifier): String {
    return resources.getString(R.string.bp_passport)
  }
}
