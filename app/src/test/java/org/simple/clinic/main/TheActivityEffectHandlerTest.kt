package org.simple.clinic.main

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant
import java.util.Optional

class TheActivityEffectHandlerTest {

  private val currentTime = Instant.parse("2018-01-01T00:00:00Z")
  private val clock = TestUtcClock(currentTime)

  private val uiActions = mock<TheActivityUiActions>()

  private val effectHandler = TheActivityEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      userSession = mock(),
      utcClock = clock,
      patientRepository = mock(),
      lockAfterTimestamp = MemoryValue(defaultValue = Optional.empty()),
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
  fun `when the show forgot PIN screen effect is received, the forgot PIN screen must be shown`() {
    // when
    testCase.dispatch(ShowForgotPinScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showForgotPinScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
