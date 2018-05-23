package org.resolvetosavelives.red.newentry.phone

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class PatientPhoneEntryScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_phone_entry
  }
}
