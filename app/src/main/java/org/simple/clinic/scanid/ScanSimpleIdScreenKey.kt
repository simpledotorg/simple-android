package org.simple.clinic.scanid

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
class ScanSimpleIdScreenKey : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName: String = "ScanSimpleId"

  override fun instantiateFragment() = ScanSimpleIdScreen()
}
