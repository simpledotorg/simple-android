package org.simple.clinic.selectstate

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData

class SelectStateUiRendererTest {

  private val ui: SelectStateUi = mock()
  private val uiRenderer = SelectStateUiRenderer(ui)

  private val defaultModel = SelectStateModel.create()

  @Test
  fun `when states list is fetched, then show states list`() {
    // given
    val states = listOf(TestData.state(displayName = "Andhra Pradesh"))
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
