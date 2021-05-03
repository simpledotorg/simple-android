package org.simple.clinic.identifiersearchresult

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
data class IdentifierSearchResultScreenKey(val shortCode: String) : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName: String = "Shortcode Search Result Screen"

  override fun instantiateFragment() = IdentifierSearchResultScreen()
}
