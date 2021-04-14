package org.simple.clinic.registration.location

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationLocationPermissionScreenKey(
    val ongoingRegistrationEntry: OngoingRegistrationEntry
) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Registration Location Permission"

  override fun layoutRes() = R.layout.screen_registration_location_permission
}
