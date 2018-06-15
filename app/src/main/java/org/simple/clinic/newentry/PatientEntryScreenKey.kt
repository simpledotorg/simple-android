package org.simple.clinic.newentry

import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class PatientEntryScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_manual_patient_entry
  }
}
