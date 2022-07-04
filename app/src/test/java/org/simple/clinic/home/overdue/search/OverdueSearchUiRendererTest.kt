package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS
import org.simple.sharedTestCode.TestData
import java.util.UUID

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
    verify(ui).hideProgress()
    verify(ui).showProgress()
    verify(ui).setOverdueSearchResultsPagingData(PagingData.empty(), searchQuery = "Ani")
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
    verify(ui).showSearchHistory(searchHistory)
    verify(ui).hideSearchResults()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search query is not empty and has no results, then display no search results`() {
    // given
    val searchHistory = setOf("Babri")
    val model = defaultModel
        .overdueSearchQueryChanged("Ani")
        .overdueSearchHistoryLoaded(searchHistory)
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideSearchHistory()
    verify(ui).hideSearchResults()
    verify(ui).showNoSearchResults()
    verify(ui).hideProgress()
    verify(ui).setOverdueSearchResultsPagingData(PagingData.empty(), searchQuery = "Ani")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when progress state is done, then render search results`() {
    // given
    val searchResults = PagingData.from(listOf(
        TestData.overdueAppointment(patientUuid = UUID.fromString("901c3195-ba1e-4fe9-9dd9-0f172a29ae4d")),
        TestData.overdueAppointment(patientUuid = UUID.fromString("e6850f5c-dc55-4bf6-9b56-a413574c8e6e"))
    ))
    val searchQuery = "Ani"
    val model = defaultModel
        .overdueSearchQueryChanged(searchQuery)
        .overdueSearchResultsLoaded(searchResults)
        .loadStateChanged(DONE)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideSearchHistory()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verify(ui).showSearchResults()
    verify(ui).setOverdueSearchResultsPagingData(searchResults, searchQuery)
    verifyNoMoreInteractions(ui)
  }
}
