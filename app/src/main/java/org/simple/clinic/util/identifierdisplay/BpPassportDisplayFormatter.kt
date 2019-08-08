package org.simple.clinic.util.identifierdisplay

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.shortcode.UuidShortCodeCreator
import org.simple.clinic.util.Unicode
import java.util.UUID
import javax.inject.Inject

class BpPassportDisplayFormatter @Inject constructor(
    private val uuidShortCodeCreator: UuidShortCodeCreator,
    private val resources: Resources
) : IdentifierDisplayAdapter.IdentifierDisplayFormatter {

  override fun formatValue(identifier: Identifier): String {
    val uuidShortCode = uuidShortCodeCreator.createFromUuid(UUID.fromString(identifier.value))

    return if (uuidShortCode.isComplete) {
      // This is guaranteed to be exactly seven characters in length.
      val prefix = uuidShortCode.shortCode.substring(0, 3)
      val suffix = uuidShortCode.shortCode.substring(3)

      "$prefix${Unicode.nonBreakingSpace}$suffix"
    } else {
      uuidShortCode.shortCode
    }
  }

  override fun formatType(identifier: Identifier): String {
    return resources.getString(R.string.identifiertype_bp_passport)
  }
}
