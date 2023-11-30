package org.simple.clinic.consent.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.MarkDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.FinishedMarkingDataProtectionConsent
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DataProtectionConsent
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class OnboardingConsentEffectHandler @Inject constructor(
    @TypedPreference(DataProtectionConsent) private val hasUserConsentedToDataProtection: Preference<Boolean>,
    private val schedulersProvider: SchedulersProvider,
) {

  fun build(): ObservableTransformer<OnboardingConsentEffect, OnboardingConsentEvent> {
    return RxMobius
        .subtypeEffectHandler<OnboardingConsentEffect, OnboardingConsentEvent>()
        .addTransformer(MarkDataProtectionConsent::class.java, markDataProtectionConsent())
        .build()
  }

  private fun markDataProtectionConsent(): ObservableTransformer<MarkDataProtectionConsent, OnboardingConsentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { hasUserConsentedToDataProtection.set(true) }
          .map { FinishedMarkingDataProtectionConsent }
    }
  }
}
