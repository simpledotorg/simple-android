package org.simple.clinic.selectstate

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.StatesResult.StatesFetched
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class SelectStateEffectHandlerTest {

  private val appConfigRepository = mock<AppConfigRepository>()
  private val effectHandler = SelectStateEffectHandler(
      appConfigRepository = appConfigRepository,
      schedulers = TestSchedulersProvider.trampoline()
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load states effect is received, then load states`() {
    // given
    val states = listOf(
        TestData.state(displayName = "Andhra Pradesh"),
        TestData.state(displayName = "Kerala")
    )

    whenever(appConfigRepository.fetchStatesInSelectedCountry()) doReturn StatesFetched(states)

    // when
    testCase.dispatch(LoadStates)

    // then
    testCase.assertOutgoingEvents(StatesResultFetched(StatesFetched(states)))
  }
}
