package org.resolvetosavelives.red.newentry.mobile

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class PatientMobileEntryScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_mobile_entry
  }
}
