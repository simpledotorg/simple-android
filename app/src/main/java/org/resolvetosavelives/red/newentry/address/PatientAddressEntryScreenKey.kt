package org.resolvetosavelives.red.newentry.address

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class PatientAddressEntryScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_address_entry
  }
}
