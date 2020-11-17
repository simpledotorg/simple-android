package org.simple.clinic.registration.register

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationLoadingScreenKey(
    val registrationEntry: OngoingRegistrationEntry
) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Ongoing Registration"

  override fun layoutRes() = R.layout.screen_registration_loading
}
