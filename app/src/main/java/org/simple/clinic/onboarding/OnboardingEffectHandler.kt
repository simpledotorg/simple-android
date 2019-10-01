package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

object OnboardingEffectHandler {
  fun createEffectHandler(
      hasUserCompletedOnboarding: Preference<Boolean>,
      ui: OnboardingUi,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<OnboardingEffect, OnboardingEvent> {
    return RxMobius
        .subtypeEffectHandler<OnboardingEffect, OnboardingEvent>()
        .addTransformer(CompleteOnboardingEffect::class.java, completeOnboardingTransformer(hasUserCompletedOnboarding))
        .addAction(MoveToRegistrationEffect::class.java, ui::moveToRegistrationScreen, schedulersProvider.ui())
        .build()
  }

  private fun completeOnboardingTransformer(
      hasUserCompletedOnboarding: Preference<Boolean>
  ): ObservableTransformer<CompleteOnboardingEffect, OnboardingEvent> {
    return ObservableTransformer { completeOnboardingEffect ->
      completeOnboardingEffect
          .doOnNext { hasUserCompletedOnboarding.set(true) }
          .map { OnboardingCompleted }
    }
  }
}
