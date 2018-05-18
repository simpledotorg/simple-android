package org.resolvetosavelives.red.newentry.bp

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class PatientBpEntryScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_bp_entry
  }
}
