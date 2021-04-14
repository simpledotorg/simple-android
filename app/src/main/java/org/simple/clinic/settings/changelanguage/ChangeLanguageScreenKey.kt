package org.simple.clinic.settings.changelanguage

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class ChangeLanguageScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Language Selection"

  override fun layoutRes(): Int = R.layout.screen_change_language
}
