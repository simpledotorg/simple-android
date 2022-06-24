package org.simple.clinic.home.overdue.search

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class OverdueSearchUiRendererTest {
  private val ui = mock<OverdueSearchUi>()
  private val uiRenderer = OverdueSearchUiRenderer(ui)
  private val defaultModel = OverdueSearchModel.create()

  @Test
  fun `when search query is empty, then clear search query and display search history and hide search results`() {
    // given
    val searchHistory = setOf("Babri")
    val model = defaultModel
        .overdueSearchQueryChanged("")
        .overdueSearchHistoryLoaded(searchHistory)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showSearchHistory(searchHistory)
    verify(ui).hideSearchResults()
    verify(ui).renderSearchQuery("")
    verifyNoMoreInteractions(ui)
  }
}
