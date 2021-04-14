package org.simple.clinic.registration.facility

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationFacilitySelectionScreenKey(
    val ongoingRegistrationEntry: OngoingRegistrationEntry
) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Registration Facility Selection"

  override fun layoutRes() = R.layout.screen_registration_facility_selection
}
