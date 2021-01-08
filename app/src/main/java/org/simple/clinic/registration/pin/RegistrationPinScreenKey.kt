package org.simple.clinic.registration.pin

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationPinScreenKey(
    val registrationEntry: OngoingRegistrationEntry
) : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName = "Registration PIN Entry"

  override fun instantiateFragment() = RegistrationPinScreen()
}
