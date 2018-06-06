package org.resolvetosavelives.red.newentry

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class PatientEntryScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_manual_patient_entry
  }
}
