package org.simple.clinic.registration.location

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class RegistrationLocationPermissionScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Registration Location Permission"

  override fun layoutRes() = R.layout.screen_registration_location_permission
}
