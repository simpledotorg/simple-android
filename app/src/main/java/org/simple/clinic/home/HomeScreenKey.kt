package org.simple.clinic.home

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
data object HomeScreenKey : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName = "Home"

  override fun instantiateFragment() = HomeScreen()
}
