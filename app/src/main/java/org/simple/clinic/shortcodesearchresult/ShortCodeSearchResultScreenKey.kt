package org.simple.clinic.shortcodesearchresult

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class ShortCodeSearchResultScreenKey(val shortCode: String) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Shortcode Search Result Screen"

  override fun layoutRes(): Int {
    return R.layout.screen_shortcode_search_result
  }
}
