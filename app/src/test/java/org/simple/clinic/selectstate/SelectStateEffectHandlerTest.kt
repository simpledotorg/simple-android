package org.simple.clinic.selectstate

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
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

  private val uiActions = mock<SelectStateUiActions>()
  private val appConfigRepository = mock<AppConfigRepository>()
  private val viewEffectHandler = SelectStateViewEffectHandler(uiActions)
  private val effectHandler = SelectStateEffectHandler(
      appConfigRepository = appConfigRepository,
      schedulers = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = viewEffectHandler::handle
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
    val deployment = TestData.deployment(
        displayName = "IHCI",
        endPoint = "https://simple.org"
    )
    val state = TestData.state(displayName = "Andhra Pradesh", deployment = deployment)

    // when
    testCase.dispatch(SaveSelectedState(state))

    // then
    testCase.assertOutgoingEvents(StateSaved)

    verify(appConfigRepository).saveState(state)
    verify(appConfigRepository).saveDeployment(deployment)
    verifyNoMoreInteractions(appConfigRepository)
  }

  @Test
  fun `when go to registration screen effect is received, then go to registration screen`() {
    // when
    testCase.dispatch(GoToRegistrationScreen)

    // then
    verify(uiActions).goToRegistrationScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when replace current screen to registration screen effect is received, then replace current screen with registration screen`() {
    // when
    testCase.dispatch(ReplaceCurrentScreenWithRegistrationScreen)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).replaceCurrentScreenToRegistrationScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
