package org.simple.clinic.instantsearch

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class InstantSearchUiRendererTest {

  private val ui = mock<InstantSearchUi>()
  private val uiRenderer = InstantSearchUiRenderer(ui)
  private val model = InstantSearchModel.create(null, null)

  @Test
  fun `when the instant search progress state is in progress, then show search progress`() {
    // given
    val loadingResultsModel = model
        .loadingSearchResults()

    // when
    uiRenderer.render(loadingResultsModel)

    // then
    verify(ui).showSearchProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the instant search progress state is done, then hide search progress`() {
    // given
    val resultsLoadedModel = model
        .searchResultsLoaded()

    // when
    uiRenderer.render(resultsLoadedModel)

    // then
    verify(ui).hideSearchProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when there is no instant search progress state, then hide search progress`() {
    // given
    val resultsLoadedModel = model
        .searchResultsLoaded()

    // when
    uiRenderer.render(resultsLoadedModel)

    // then
    verify(ui).hideSearchProgress()
    verifyNoMoreInteractions(ui)
  }
}
