package org.simple.clinic.security.pin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class PinEntryEffectHandlerTest {

  private val uiActions = mock<UiActions>()

  private val effectHandler = PinEntryEffectHandler(
      bruteForceProtection = mock(),
      schedulersProvider = TrampolineSchedulersProvider(),
      uiActions = uiActions,
      pinVerificationMethod = mock()
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the show network error effect is received, the network error must be shown`() {
    // when
    testCase.dispatch(ShowNetworkError)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showNetworkError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the show server error effect is received, the server error must be shown`() {
    // when
    testCase.dispatch(ShowServerError)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showServerError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the show unexpected error effect is received, the generic error must be shown`() {
    // when
    testCase.dispatch(ShowUnexpectedError)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(uiActions)
  }
}
