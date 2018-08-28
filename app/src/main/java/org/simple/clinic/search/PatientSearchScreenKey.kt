package org.simple.clinic.search

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class PatientSearchScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Patient Search"

  override fun layoutRes(): Int {
    return R.layout.screen_patient_search
  }
}
