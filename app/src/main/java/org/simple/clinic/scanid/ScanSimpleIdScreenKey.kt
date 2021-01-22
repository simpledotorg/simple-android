package org.simple.clinic.scanid

import kotlinx.android.parcel.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
class ScanSimpleIdScreenKey : ScreenKey() {

  override val analyticsName: String = "ScanSimpleId"

  override fun instantiateFragment() = ScanSimpleIdScreen()
}
