package org.simple.clinic.search

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class PatientSearchScreenKey(
    val additionalIdentifier: Identifier?
) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Patient Search"

  override fun layoutRes(): Int {
    return R.layout.screen_patient_search
  }
}
