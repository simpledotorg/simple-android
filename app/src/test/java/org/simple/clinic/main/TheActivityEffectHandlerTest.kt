package org.simple.clinic.main

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.empty.EmptyScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.navigation.v2.History
import org.simple.clinic.navigation.v2.Normal
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.storage.MemoryValue
import org.simple.sharedTestCode.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant
import java.util.Optional

class TheActivityEffectHandlerTest {

  private val currentTime = Instant.parse("2018-01-01T00:00:00Z")
  private val clock = TestUtcClock(currentTime)

  private val uiActions = mock<TheActivityUiActions>()
  private val currentHistory = History(listOf(Normal(EmptyScreenKey().wrap())))
  private val effectHandler = TheActivityEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      userSession = mock(),
      utcClock = clock,
      patientRepository = mock(),
      lockAfterTimestamp = MemoryValue(defaultValue = Optional.empty()),
      uiActions = uiActions,
      provideCurrentScreenHistory = { currentHistory }
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the set current screen history effect is received, the current screen history must be replaced`() {
    // given
    val history = History(listOf(
        Normal(EmptyScreenKey().wrap()),
        Normal(HomeScreenKey)
    ))

    // when
    testCase.dispatch(SetCurrentScreenHistory(history))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).setCurrentScreenHistory(history)
    verifyNoMoreInteractions(uiActions)
  }
}
