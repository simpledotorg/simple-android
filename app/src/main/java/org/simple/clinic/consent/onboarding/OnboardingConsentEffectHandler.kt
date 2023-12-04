package org.simple.clinic.consent.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.CompleteOnboardingEffect
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.MarkDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.FinishedMarkingDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.OnboardingCompleted
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DataProtectionConsent
import org.simple.clinic.util.scheduler.SchedulersProvider

class OnboardingConsentEffectHandler @AssistedInject constructor(
    @TypedPreference(DataProtectionConsent) private val hasUserConsentedToDataProtection: Preference<Boolean>,
    @TypedPreference(TypedPreference.Type.OnboardingComplete) private val hasUserCompletedOnboarding: Preference<Boolean>,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<OnboardingConsentViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<OnboardingConsentViewEffect>): OnboardingConsentEffectHandler
  }

  fun build(): ObservableTransformer<OnboardingConsentEffect, OnboardingConsentEvent> {
    return RxMobius
        .subtypeEffectHandler<OnboardingConsentEffect, OnboardingConsentEvent>()
        .addTransformer(MarkDataProtectionConsent::class.java, markDataProtectionConsent())
        .addConsumer(OnboardingConsentViewEffect::class.java, viewEffectsConsumer::accept)
        .addTransformer(CompleteOnboardingEffect::class.java, completeOnboardingEffect())
        .build()
  }

  private fun completeOnboardingEffect(): ObservableTransformer<CompleteOnboardingEffect, OnboardingConsentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { hasUserCompletedOnboarding.set(true) }
          .map { OnboardingCompleted }
    }
  }

  private fun markDataProtectionConsent(): ObservableTransformer<MarkDataProtectionConsent, OnboardingConsentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { hasUserConsentedToDataProtection.set(true) }
          .map { FinishedMarkingDataProtectionConsent }
    }
  }
}
