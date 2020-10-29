package org.simple.clinic.registerorlogin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class AuthenticationEffectHandlerTest {

  private val uiActions = mock<AuthenticationUiActions>()

  private val effectHandler = AuthenticationEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when receiving the open country selection screen effect, open the country selection screen`() {
    // when
    testCase.dispatch(OpenCountrySelectionScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openCountrySelectionScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when receiving the open registration phone screen effect, open the registration phone screen`() {
    // when
    testCase.dispatch(OpenRegistrationPhoneScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openRegistrationPhoneScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
