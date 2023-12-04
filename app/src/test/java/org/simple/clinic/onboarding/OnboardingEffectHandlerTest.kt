package org.simple.clinic.onboarding

import org.junit.After
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.clinic.mobius.EffectHandlerTestCase

class OnboardingEffectHandlerTest {

  private val uiActions = mock<OnboardingUi>()

  private val effectHandler = OnboardingEffectHandler(
      viewEffectsConsumer = OnboardingViewEffectHandler(uiActions)::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
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
