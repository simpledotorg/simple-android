package org.resolvetosavelives.red.newentry.drugs

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class PatientCurrentDrugsEntryScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_drugs_entry
  }
}
