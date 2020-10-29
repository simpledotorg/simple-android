package org.simple.clinic.empty

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class EmptyScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Empty Screen"

  override fun layoutRes(): Int {
    return R.layout.screen_empty
  }
}
