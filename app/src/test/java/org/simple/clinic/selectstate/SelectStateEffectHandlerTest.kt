package org.simple.clinic.selectstate

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.StatesResult
import org.simple.clinic.appconfig.StatesResult.FetchError
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.ResolvedError.NetworkRelated
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
  fun `when loading states is successful, then states fetched event should be emitted`() {
    // given
    val states = listOf(
        TestData.state(displayName = "Andhra Pradesh"),
        TestData.state(displayName = "Kerala")
    )

    whenever(appConfigRepository.fetchStatesInSelectedCountry()) doReturn StatesResult.StatesFetched(states)

    // when
    testCase.dispatch(LoadStates)

    // then
    testCase.assertOutgoingEvents(StatesFetched(states))
  }

  @Test
  fun `when loading states fails, then failed to fetch states event should be emitted`() {
    // given
    val error = StatesFetchError.NetworkError

    whenever(appConfigRepository.fetchStatesInSelectedCountry()) doReturn FetchError(NetworkRelated(RuntimeException()))

    // when
    testCase.dispatch(LoadStates)

    // then
    testCase.assertOutgoingEvents(FailedToFetchStates(error))
  }

  @Test
  fun `when save state effect is received, then save state to local persistence`() {
    // given
    val state = TestData.state(displayName = "Andhra Pradesh")

    // when
    testCase.dispatch(SaveSelectedState(state))

    // then
    testCase.assertOutgoingEvents(StateSaved)
  }
}
