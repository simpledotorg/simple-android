package org.simple.clinic.util.identifierdisplay

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.patient.businessid.Identifier
import javax.inject.Inject

class BangladeshNationalIdDisplayFormatter @Inject constructor(
    private val resources: Resources
) : IdentifierDisplayAdapter.IdentifierDisplayFormatter {

  override fun formatValue(identifier: Identifier): String = identifier.value

  override fun formatType(identifier: Identifier): String {
    return resources.getString(R.string.identifiertype_bangladesh_national_id)
  }
}
