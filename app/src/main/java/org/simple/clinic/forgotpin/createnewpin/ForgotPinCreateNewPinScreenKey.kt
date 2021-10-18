package org.simple.clinic.forgotpin.createnewpin

import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey

@Parcelize
data class ForgotPinCreateNewPinScreenKey(
    override val analyticsName: String = "Forgot PIN Create New PIN"
) : FullScreenKey {

  override fun layoutRes() = R.layout.screen_forgotpin_createpin
}
