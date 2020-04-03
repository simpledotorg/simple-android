package org.simple.clinic.util.identifierdisplay

import android.content.res.Resources
import org.simple.clinic.patient.businessid.Identifier
import javax.inject.Inject

/**
 * We need to render items of [Identifier] type in the UI, sometimes differently from how
 * they are represented in code.
 *
 * For example, the short code of a [Identifier.IdentifierType.BpPassport] is represented as a
 * 7-digit string, ex ("1234567"), but when rendering it in the UI we need to display it as
 * "123 4567". We do not want to store these strings with the spaces in business layer because this
 * is a presentation detail.
 *
 * This class is a helper class that is meant to convert [Identifier] instances to their display
 * formats.
 **/
class IdentifierDisplayAdapter @Inject constructor(
    private val resources: Resources
) {

  fun valueAsText(identifier: Identifier): String {
    return identifier.displayValue()
  }

  fun typeAsText(identifier: Identifier): String {
    return identifier.displayType(resources)
  }
}
