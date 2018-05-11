package org.resolvetosavelives.red.newentry.success

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class PatientSavedScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_saved
  }
}
