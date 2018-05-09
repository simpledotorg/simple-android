package org.resolvetosavelives.red.newentry.mobile

import android.annotation.SuppressLint
import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
@SuppressLint("ParcelCreator")
class PatientMobileEntryScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_mobile_entry
  }
}
