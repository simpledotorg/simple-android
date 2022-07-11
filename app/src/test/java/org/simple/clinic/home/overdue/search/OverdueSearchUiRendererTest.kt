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
    verify(ui).setOverdueSearchResultsPagingData(PagingData.empty(), emptySet(), searchQuery = "Ani")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search query is empty and has no results, then display search history and hide download and share buttons`() {
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
    verify(ui).hideDownloadAndShareButtons()
    verify(ui).hideSearchResults()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search query is not empty and has no results, then display no search results and hide download and share buttons`() {
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
    verify(ui).setOverdueSearchResultsPagingData(PagingData.empty(), emptySet(), searchQuery = "Ani")
    verify(ui).hideDownloadAndShareButtons()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when progress state is done, then render search results and show download and share buttons`() {
    // given
    val searchResults = PagingData.from(listOf(
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("901c3195-ba1e-4fe9-9dd9-0f172a29ae4d"),
            appointment = TestData.appointment(
                uuid = UUID.fromString("d8924174-6109-4695-87a1-2c19a929eeb0")
            )
        ),
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("e6850f5c-dc55-4bf6-9b56-a413574c8e6e"),
            appointment = TestData.appointment(
                uuid = UUID.fromString("c888b373-7d55-4eec-96b7-8ff4d5970138")
            )
        )
    ))
    val selectedAppointments = setOf(UUID.fromString("d8924174-6109-4695-87a1-2c19a929eeb0"))
    val searchQuery = "Ani"
    val model = defaultModel
        .overdueSearchQueryChanged(searchQuery)
        .overdueSearchResultsLoaded(searchResults)
        .loadStateChanged(DONE)
        .selectedOverdueAppointmentsChanged(selectedAppointments)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideSearchHistory()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verify(ui).showSearchResults()
    verify(ui).setOverdueSearchResultsPagingData(searchResults, selectedAppointments, searchQuery)
    verify(ui).showDownloadAndShareButtons()
    verifyNoMoreInteractions(ui)
  }
}
