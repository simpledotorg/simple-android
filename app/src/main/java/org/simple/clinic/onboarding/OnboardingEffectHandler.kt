package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

object OnboardingEffectHandler {
  fun createEffectHandler(
      hasUserCompletedOnboarding: Preference<Boolean>,
      ui: OnboardingUi
  ): ObservableTransformer<OnboardingEffect, OnboardingEvent> {
    return RxMobius
        .subtypeEffectHandler<OnboardingEffect, OnboardingEvent>()
        .addTransformer(CompleteOnboardingEffect::class.java) { completeOnboardingEffect ->
          completeOnboardingEffect
              .doOnNext { hasUserCompletedOnboarding.set(true) }
              .map { OnboardingCompleted }
        }
        .addAction(MoveToRegistrationEffect::class.java) { ui.moveToRegistrationScreen() }
        .build()
  }
}
