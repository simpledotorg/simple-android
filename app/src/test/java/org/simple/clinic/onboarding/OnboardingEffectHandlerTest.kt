package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
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
  fun `when move to registration effect is received, then move to registration screen`() {
    // when
    testCase.dispatch(MoveToRegistrationEffect)

    // then
    verify(uiActions).moveToRegistrationScreen()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }
}
