package org.simple.clinic.home.report

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class ReportsScreenKey  : FullScreenKey, Parcelable{

  @IgnoredOnParcel
  override val analyticsName = "Reports"

  override fun layoutRes() = R.layout.screen_report
}
