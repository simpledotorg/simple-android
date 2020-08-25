package org.simple.clinic.home

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
object HomeScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Home"

  override fun layoutRes(): Int {
    return R.layout.screen_home
  }
}
