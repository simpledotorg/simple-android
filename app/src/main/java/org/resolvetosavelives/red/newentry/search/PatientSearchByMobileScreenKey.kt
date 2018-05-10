package org.resolvetosavelives.red.newentry.search

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class PatientSearchByMobileScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_search_by_mobile
  }
}
