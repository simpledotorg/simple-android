package org.simple.clinic.registration.phone

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class RegistrationPhoneScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Registration Phone Entry"

  override fun layoutRes() = R.layout.screen_registration_phone
}
