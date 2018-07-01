package org.simple.clinic.qrscan

import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class AadhaarScanScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_aadhaar_scanner
  }
}
