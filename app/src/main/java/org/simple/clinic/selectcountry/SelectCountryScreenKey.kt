package org.simple.clinic.selectcountry

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey

@Parcelize
class SelectCountryScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Select Country"

  override fun layoutRes(): Int = R.layout.screen_selectcountry
}
