package org.simple.clinic.scanid

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class ScanSimpleIdScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "ScanSimpleId"

  override fun layoutRes() = R.layout.screen_scan_simple
}
