package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase

class OnboardingEffectHandlerTest {

  private val uiActions = mock<OnboardingUi>()
  private val hasUserCompletedOnboarding = mock<Preference<Boolean>>()

  private val effectHandler = OnboardingEffectHandler(
      hasUserCompletedOnboarding = hasUserCompletedOnboarding,
      viewEffectsConsumer = OnboardingViewEffectHandler(uiActions)::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when complete onboarding effect is received, then mark user has completed onboarding`() {
    // when
    testCase.dispatch(CompleteOnboardingEffect)

    // then
    verify(hasUserCompletedOnboarding).set(eq(true))
    testCase.assertOutgoingEvents(OnboardingCompleted)
  }

  @Test
  fun `when open onboarding consent screen effect is received, then open onboarding consent screen`() {
    // when
    testCase.dispatch(OpenOnboardingConsentScreen)

    // then
    verify(uiActions).openOnboardingConsentScreen()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }
}
