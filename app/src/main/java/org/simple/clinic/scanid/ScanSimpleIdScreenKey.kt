package org.simple.clinic.scanid

import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
data class ScanSimpleIdScreenKey(
    val openedFrom: OpenedFrom,
    override val analyticsName: String = "ScanSimpleId"
) : ScreenKey() {

  override fun instantiateFragment() = ScanSimpleIdScreen()
}
