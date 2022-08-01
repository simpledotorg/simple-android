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
  private val uiRenderer = OverdueSearchUiRenderer(
      ui = ui,
      isOverdueSearchV2Enabled = false
  )
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
    verify(ui).showDownloadAndShareButtons()
    verify(ui).showSelectedOverdueAppointmentCount(1)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when overdue appointments are selected, then show overdue selected count`() {
    // given
    val searchResults = PagingData.from(listOf(
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("45c8f51e-9e32-433a-82db-4cef17d836fd"),
            appointment = TestData.appointment(
                uuid = UUID.fromString("88d9643f-5533-4137-a522-03e2717bea69")
            )
        ),
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("433b3c36-4f28-4a40-aede-93a5a7d36ed2"),
            appointment = TestData.appointment(
                uuid = UUID.fromString("dd738d46-ef9e-450f-9430-cf0b0fdffe53")
            )
        )
    ))
    val selectedAppointments = setOf(UUID.fromString("4ab9f2ee-64a0-48c9-99c4-35f46c2e43a4"))
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
    verify(ui).showDownloadAndShareButtons()
    verify(ui).showSelectedOverdueAppointmentCount(1)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when no overdue appointments are selected, then hide overdue selected count`() {
    // given
    val searchResults = PagingData.from(listOf(
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("5a319d3c-cee1-4914-8f1e-c077c37ec9a3"),
            appointment = TestData.appointment(
                uuid = UUID.fromString("042613d8-d2a3-4e1b-aa16-692480a745e0")
            )
        ),
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("cef95686-12fb-46af-a25a-2b3842a7867d"),
            appointment = TestData.appointment(
                uuid = UUID.fromString("9c206891-e4c1-4f72-a72b-a03ac0ae021f")
            )
        )
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
    verify(ui).showDownloadAndShareButtons()
    verify(ui).hideSelectedOverdueAppointmentCount()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search suggestions are loaded, then set overdue search suggestions`() {
    // given
    val villageAndPatientNames = listOf("Babri", "Narwar", "Anand Nagar")
    val searchSuggestionsModel = defaultModel
        .villagesAndPatientNamesLoaded(villageAndPatientNames)
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(searchSuggestionsModel)

    // then
    verify(ui).setOverdueSearchSuggestions(villageAndPatientNames)
    verify(ui).showSearchHistory(emptySet())
    verify(ui).hideDownloadAndShareButtons()
    verify(ui).hideSearchResults()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search query is empty and has no results and overdue search v2 is enabled, then don't display search history and hide download and share buttons`() {
    // given
    val searchHistory = setOf("Babri")
    val model = defaultModel
        .overdueSearchQueryChanged("")
        .overdueSearchHistoryLoaded(searchHistory)
        .loadStateChanged(NO_RESULTS)

    val uiRenderer = OverdueSearchUiRenderer(
        ui = ui,
        isOverdueSearchV2Enabled = true
    )

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideSearchHistory()
    verify(ui).hideDownloadAndShareButtons()
    verify(ui).hideSearchResults()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }
}
