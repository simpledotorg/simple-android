package org.simple.clinic.settings

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey

@Parcelize
class SettingsScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Settings"

  override fun layoutRes(): Int = R.layout.screen_settings
}
