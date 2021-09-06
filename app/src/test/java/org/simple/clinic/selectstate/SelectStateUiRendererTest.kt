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
    verifyNoMoreInteractions(ui)
  }
}
