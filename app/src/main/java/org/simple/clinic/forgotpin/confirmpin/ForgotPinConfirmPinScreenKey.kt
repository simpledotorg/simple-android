package org.simple.clinic.forgotpin.confirmpin

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey

@Parcelize
data class ForgotPinConfirmPinScreenKey(val enteredPin: String) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Forgot PIN Confirm PIN"

  override fun layoutRes() = R.layout.screen_forgotpin_confirmpin
}
