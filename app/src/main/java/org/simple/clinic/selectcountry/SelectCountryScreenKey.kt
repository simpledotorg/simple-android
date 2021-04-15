package org.simple.clinic.selectcountry

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class SelectCountryScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Select Country"

  override fun layoutRes(): Int = R.layout.screen_selectcountry
}
