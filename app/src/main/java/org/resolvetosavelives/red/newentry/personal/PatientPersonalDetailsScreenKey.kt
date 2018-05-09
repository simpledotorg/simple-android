package org.resolvetosavelives.red.newentry.personal

import android.annotation.SuppressLint
import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
@SuppressLint("ParcelCreator")
class PatientPersonalDetailsScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_personal_details_entry
  }
}
