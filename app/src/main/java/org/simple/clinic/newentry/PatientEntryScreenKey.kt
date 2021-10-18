package org.simple.clinic.newentry

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey

@Parcelize
class PatientEntryScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Create Patient Entry"

  override fun layoutRes(): Int {
    return R.layout.screen_manual_patient_entry
  }
}
