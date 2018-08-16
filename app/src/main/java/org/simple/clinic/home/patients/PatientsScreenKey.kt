package org.simple.clinic.home.patients

import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class PatientsScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patients
  }
}
