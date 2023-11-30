package org.simple.clinic.consent.onboarding

import com.f2prateek.rx.preferences2.Preference
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.CompleteOnboardingEffect
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.MarkDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.FinishedMarkingDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.OnboardingCompleted
import org.simple.clinic.consent.onboarding.OnboardingConsentViewEffect.MoveToRegistrationActivity
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class OnboardingConsentEffectHandlerTest {

  private val hasUserConsentedToDataProtection = mock<Preference<Boolean>>()
  private val hasUserCompletedOnboarding = mock<Preference<Boolean>>()
  private val uiActions = mock<UiActions>()

  private val effectHandlerTestCase = EffectHandlerTestCase(
      OnboardingConsentEffectHandler(
          hasUserConsentedToDataProtection = hasUserConsentedToDataProtection,
          hasUserCompletedOnboarding = hasUserCompletedOnboarding,
          schedulersProvider = TestSchedulersProvider.trampoline(),
          viewEffectsConsumer = OnboardingConsentViewEffectHandler(uiActions)::handle
      ).build()
  )

  @Test
  fun `when mark consent effect is received, then mark data protection consent`() {
    // when
    effectHandlerTestCase.dispatch(MarkDataProtectionConsent)

    // then
    verify(hasUserConsentedToDataProtection).set(true)
    verifyNoMoreInteractions(hasUserConsentedToDataProtection)

    effectHandlerTestCase.assertOutgoingEvents(FinishedMarkingDataProtectionConsent)
  }

  @Test
  fun `when move to registration activity view effect is received, then move to registration activity`() {
    // when
    effectHandlerTestCase.dispatch(MoveToRegistrationActivity)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).moveToRegistrationActivity()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when complete onboarding effect is received, then complete onboarding`() {
    // when
    effectHandlerTestCase.dispatch(CompleteOnboardingEffect)

    // then
    verify(hasUserCompletedOnboarding).set(true)
    verifyNoMoreInteractions(hasUserCompletedOnboarding)

    effectHandlerTestCase.assertOutgoingEvents(OnboardingCompleted)
  }
}
