package org.simple.clinic.deniedaccess

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
data class AccessDeniedScreenKey(val fullName: String) : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName = "Access Denied"

  override fun instantiateFragment() = AccessDeniedScreen()
}
