package org.simple.clinic.onboarding

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

object OnboardingEffectHandler {
  fun createEffectHandler(): ObservableTransformer<OnboardingEffect, OnboardingEvent> {
    return RxMobius
        .subtypeEffectHandler<OnboardingEffect, OnboardingEvent>()
        .build()
  }
}
