package org.simple.clinic.main

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class TheActivityEffectHandlerTest {

  private val uiActions = mock<TheActivityUiActions>()

  private val effectHandler = TheActivityEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      userSession = mock(),
      utcClock = TestUtcClock(),
      patientRepository = mock(),
      lockAfterTimestamp = MemoryValue(Optional.empty()),
      uiActions = uiActions
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the show home screen effect is received, the home screen must be shown`() {
    // when
    testCase.dispatch(ShowHomeScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showHomeScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the show forgot pin create pin screen effect is received, the forgot pin create new pin screen must be shown`() {
    // when
    testCase.dispatch(ShowForgotPinCreatePinScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showForgotPinCreateNewPinScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
