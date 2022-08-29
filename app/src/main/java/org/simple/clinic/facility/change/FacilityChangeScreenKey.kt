package org.simple.clinic.facility.change

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey

@Parcelize
class FacilityChangeScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Post Registration Facility Change"

  override fun layoutRes() = R.layout.screen_facility_change
}
