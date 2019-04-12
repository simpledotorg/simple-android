package org.simple.clinic.util.identifierdisplay

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.patient.businessid.Identifier
import javax.inject.Inject

class UnknownDisplayFormatter @Inject constructor(
    private val resources: Resources
) : IdentifierDisplayAdapter.IdentifierDisplayFormatter {

  override fun formatValue(identifier: Identifier) = identifier.value

  override fun formatType(identifier: Identifier): String = resources.getString(R.string.identifiertype_unknown)
}
