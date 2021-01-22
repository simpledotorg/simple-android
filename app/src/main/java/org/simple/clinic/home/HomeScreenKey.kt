package org.simple.clinic.home

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
object HomeScreenKey : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName = "Home"

  override fun instantiateFragment() = HomeScreen()
}
