package org.simple.clinic.home.help

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class HelpScreenKey  : FullScreenKey, Parcelable{

  @IgnoredOnParcel
  override val analyticsName = "Help"

  override fun layoutRes() = R.layout.screen_help
}
