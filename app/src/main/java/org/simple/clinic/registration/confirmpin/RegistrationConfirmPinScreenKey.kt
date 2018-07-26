package org.simple.clinic.registration.confirmpin

import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class RegistrationConfirmPinScreenKey : FullScreenKey {

  override fun layoutRes() = R.layout.screen_registration_confirm_pin
}
