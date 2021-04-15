package org.simple.clinic.recentpatient

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class RecentPatientsScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Recent Patient Screen"

  override fun layoutRes() = R.layout.recent_patients_screen
}
