package org.simple.clinic.home.overdue.search

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class OverdueSearchUiRendererTest {

  @Test
  fun `when search query is empty, then display search history and hide search results`() {
    // given
    val ui = mock<OverdueSearchUi>()
    val uiRenderer = OverdueSearchUiRenderer(ui)
    val searchHistory = setOf("Babri")
    val model = OverdueSearchModel
        .create()
        .overdueSearchQueryChanged("")
        .overdueSearchHistoryLoaded(searchHistory)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showSearchHistory(searchHistory)
    verify(ui).hideSearchResults()
  }
}
