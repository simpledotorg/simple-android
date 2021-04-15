package org.simple.clinic.registration.confirmpin

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationConfirmPinScreenKey(val registrationEntry: OngoingRegistrationEntry) : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName = "Registration Confirm PIN"

  override fun instantiateFragment() = RegistrationConfirmPinScreen()
}
