package org.simple.clinic.settings

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class SettingsScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Settings"

  override fun layoutRes(): Int = R.layout.screen_settings
}
