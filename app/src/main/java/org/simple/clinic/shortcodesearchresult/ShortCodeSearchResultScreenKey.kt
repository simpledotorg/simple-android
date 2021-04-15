package org.simple.clinic.shortcodesearchresult

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
data class ShortCodeSearchResultScreenKey(val shortCode: String) : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName: String = "Shortcode Search Result Screen"

  override fun instantiateFragment() = ShortCodeSearchResultScreen()
}
