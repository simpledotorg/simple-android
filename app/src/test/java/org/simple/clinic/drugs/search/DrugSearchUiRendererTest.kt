package org.simple.clinic.drugs.search

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test

class DrugSearchUiRendererTest {

  private val ui = mock<DrugSearchUi>()
  private val uiRenderer = DrugSearchUiRenderer(ui)

  private val defaultModel = DrugSearchModel.create()

  @Test
  fun `when search query is empty, then hide search results`() {
    // given
    val emptySearchQueryModel = defaultModel
        .searchQueryChanged(searchQuery = "")

    // when
    uiRenderer.render(emptySearchQueryModel)

    // then
    verify(ui).hideSearchResults()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search query is not empty, then show search results`() {
    // given
    val searchQueryModel = defaultModel
        .searchQueryChanged(searchQuery = "Amlodipine")

    // when
    uiRenderer.render(searchQueryModel)

    // then
    verify(ui).showSearchResults()
    verifyNoMoreInteractions(ui)
  }
}
