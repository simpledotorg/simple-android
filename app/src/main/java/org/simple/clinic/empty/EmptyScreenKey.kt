package org.simple.clinic.empty

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey

@Parcelize
class EmptyScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Empty Screen"

  override fun layoutRes(): Int {
    return R.layout.screen_empty
  }
}
