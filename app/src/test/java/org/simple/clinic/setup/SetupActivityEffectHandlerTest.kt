package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class SetupActivityEffectHandlerTest {

  private val onboardingCompletePreference = mock<Preference<Boolean>>()

  private val effectHandler = SetupActivityEffectHandler.create(onboardingCompletePreference, TrampolineSchedulersProvider())
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `the user details must be fetched when the fetch user details effect is received`() {
    // given
    whenever(onboardingCompletePreference.get()).doReturn(true)

    // when
    testCase.dispatch(FetchUserDetails)

    // then
    testCase.assertOutgoingEvents(UserDetailsFetched(hasUserCompletedOnboarding = true))
  }
}
