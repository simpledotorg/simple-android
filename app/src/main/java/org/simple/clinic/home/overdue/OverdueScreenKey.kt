package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class OverdueScreenKey : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "Overdue"

  override fun layoutRes() = R.layout.screen_overdue
}
