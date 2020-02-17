package org.simple.clinic.deniedaccess

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class AccessDeniedScreenKey(val fullName: String) : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "Access Denied"

  override fun layoutRes() = R.layout.screen_access_denied
}
