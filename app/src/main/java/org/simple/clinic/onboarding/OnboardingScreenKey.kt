package org.simple.clinic.onboarding

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class OnboardingScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Onboarding"

  override fun layoutRes() = R.layout.screen_onboarding
}
