package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

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

  @Test
  fun `when overdue search results are loaded, then display overdue search results and hide overdue search history`() {
    // given
    val facilityUuid = UUID.fromString("7dba16a0-1090-41f6-8e0c-0d97989de898")
    val overdueAppointments = listOf(TestData.overdueAppointment(
        facilityUuid = facilityUuid,
        name = "Anish Acharya",
        patientUuid = UUID.fromString("37259e96-e757-4608-aeae-f1a20b088f09")
    ), TestData.overdueAppointment(
        facilityUuid = facilityUuid,
        name = "Anirban Dar",
        patientUuid = UUID.fromString("53659148-a157-4aa4-92fb-c0a7991ae872")
    ))

    val overdueSearchResults = PagingData.from(overdueAppointments)

    val overdueSearchResultsModel = defaultModel
        .overdueSearchQueryChanged("Ani")
        .overdueSearchResultsLoaded(overdueSearchResults)

    // when
    uiRenderer.render(overdueSearchResultsModel)

    // then
    verify(ui).showOverdueSearchResults(overdueSearchResults)
    verify(ui).hideSearchHistory()
    verify(ui).renderSearchQuery("Ani")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search query is not empty but search results have not loaded, then hide search history and search results`() {
    // given
    val searchQueryLoadedModel = defaultModel.overdueSearchQueryChanged("Ani")

    // when
    uiRenderer.render(searchQueryLoadedModel)

    // then
    verify(ui).hideSearchHistory()
    verify(ui).hideSearchResults()
    verify(ui).renderSearchQuery("Ani")
    verifyNoMoreInteractions(ui)
  }
}
