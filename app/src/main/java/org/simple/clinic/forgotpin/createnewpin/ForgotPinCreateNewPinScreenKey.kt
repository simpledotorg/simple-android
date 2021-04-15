package org.simple.clinic.forgotpin.createnewpin

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class ForgotPinCreateNewPinScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Forgot PIN Create New PIN"

  override fun layoutRes() = R.layout.screen_forgotpin_createpin
}
