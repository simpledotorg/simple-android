package org.simple.clinic.registration.name

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationNameScreenKey(
    val registrationEntry: OngoingRegistrationEntry
) : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName = "Registration Name Entry"

  override fun instantiateFragment() = RegistrationFullNameScreen()
}
