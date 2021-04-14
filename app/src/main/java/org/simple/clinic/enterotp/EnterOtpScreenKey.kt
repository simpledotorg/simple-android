package org.simple.clinic.enterotp

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class EnterOtpScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Enter Login OTP Manually"

  override fun layoutRes() = R.layout.screen_enterotp
}
