package org.simple.clinic.enterotp

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey

@Parcelize
class EnterOtpScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Enter Login OTP Manually"

  override fun layoutRes() = R.layout.screen_enterotp
}
