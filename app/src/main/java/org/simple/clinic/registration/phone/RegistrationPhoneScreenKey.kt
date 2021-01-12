package org.simple.clinic.registration.phone

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
class RegistrationPhoneScreenKey : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName = "Registration Phone Entry"

  override fun instantiateFragment() = RegistrationPhoneScreen()
}
