package org.simple.clinic.home.overdue.search

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS

class OverdueSearchUiRendererTest {
  private val ui = mock<OverdueSearchUi>()
  private val uiRenderer = OverdueSearchUiRenderer(ui)
  private val defaultModel = OverdueSearchModel.create()

  @Test
  fun `when progress state is in progress, then show progress`() {
    // given
    val model = defaultModel
        .overdueSearchQueryChanged("Ani")
        .loadStateChanged(IN_PROGRESS)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderSearchQuery("Ani")
    verify(ui).hideProgress()
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search query is empty and has no results, then display search history`() {
    // given
    val searchHistory = setOf("Babri")
    val model = defaultModel
        .overdueSearchQueryChanged("")
        .overdueSearchHistoryLoaded(searchHistory)
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderSearchQuery("")
    verify(ui).showSearchHistory(searchHistory)
    verify(ui).hideSearchResults()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `search query is not empty and has no results, then display no search results`() {
    // given
    val searchHistory = setOf("Babri")
    val model = defaultModel
        .overdueSearchQueryChanged("Ani")
        .overdueSearchHistoryLoaded(searchHistory)
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderSearchQuery("Ani")
    verify(ui).hideSearchHistory()
    verify(ui).hideSearchResults()
    verify(ui).showNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when progress state is done, then render search results`() {
    // given
    val searchHistory = setOf("Babri")
    val model = defaultModel
        .overdueSearchQueryChanged("Ani")
        .overdueSearchHistoryLoaded(searchHistory)
        .loadStateChanged(DONE)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderSearchQuery("Ani")
    verify(ui).hideSearchHistory()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verify(ui).showSearchResults()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when there is no progress state, then render search history`() {
    // given
    val searchHistory = setOf("Babri")
    val model = defaultModel
        .overdueSearchQueryChanged("")
        .overdueSearchHistoryLoaded(searchHistory)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderSearchQuery("")
    verify(ui).showSearchHistory(searchHistory)
    verify(ui).hideSearchResults()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }
}
