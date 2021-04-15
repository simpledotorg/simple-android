package org.simple.clinic.newentry

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class PatientEntryScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Create Patient Entry"

  override fun layoutRes(): Int {
    return R.layout.screen_manual_patient_entry
  }
}
