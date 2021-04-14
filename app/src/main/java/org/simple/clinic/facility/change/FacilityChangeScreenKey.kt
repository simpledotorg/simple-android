package org.simple.clinic.facility.change

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class FacilityChangeScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Post Registration Facility Change"

  override fun layoutRes() = R.layout.screen_facility_change
}
