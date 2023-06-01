package org.simple.clinic.selectstate

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData

class SelectStateUiRendererTest {

  private val ui: SelectStateUi = mock()
  private val uiRenderer = SelectStateUiRenderer(ui)

  private val defaultModel = SelectStateModel.create()

  @Test
  fun `when states list is fetched, then show states list`() {
    // given
    val andhraPradesh = TestData.state(displayName = "Andhra Pradesh")
    val maharashtra = TestData.state(displayName = "Maharashtra")
    val states = listOf(andhraPradesh, maharashtra)
    val statesLoadedModel = defaultModel
        .statesLoaded(states)

    // when
    uiRenderer.render(statesLoadedModel)

    // then
    verify(ui).showStates(states = states, selectedState = null)
    verify(ui).hideErrorView()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when states are failed to load with network error, then show network error message`() {
    // given
    val failedToLoadStatesModel = defaultModel
        .failedToLoadStates(StatesFetchError.NetworkError)

    // when
    uiRenderer.render(failedToLoadStatesModel)

    // then
    verify(ui).hideStates()
    verify(ui).showNetworkErrorMessage()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when states are failed to load with server error, then show server error message`() {
    // given
    val failedToLoadStatesModel = defaultModel
        .failedToLoadStates(StatesFetchError.ServerError)

    // when
    uiRenderer.render(failedToLoadStatesModel)

    // then
    verify(ui).hideStates()
    verify(ui).showServerErrorMessage()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when states are failed to load with unexpected error, then show generic error message`() {
    // given
    val failedToLoadStatesModel = defaultModel
        .failedToLoadStates(StatesFetchError.UnexpectedError)

    // when
    uiRenderer.render(failedToLoadStatesModel)

    // then
    verify(ui).hideStates()
    verify(ui).showGenericErrorMessage()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when states are being fetched, then show the progress bar`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showProgress()
    verify(ui).hideStates()
    verify(ui).hideErrorView()
    verifyNoMoreInteractions(ui)
  }
}
