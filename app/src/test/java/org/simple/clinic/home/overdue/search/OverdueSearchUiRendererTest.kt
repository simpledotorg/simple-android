package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS
import org.simple.sharedTestCode.TestData
import java.util.UUID

class OverdueSearchUiRendererTest {
  private val ui = mock<OverdueSearchUi>()
  private val uiRenderer = OverdueSearchUiRenderer(
      ui = ui
  )
  private val defaultModel = OverdueSearchModel.create()

  @Test
  fun `when progress state is in progress, then show progress`() {
    // given
    val model = defaultModel
        .overdueSearchInputsChanged(searchInputs = listOf("Ani"))
        .loadStateChanged(IN_PROGRESS)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setOverdueSearchResultsPagingData(
        overdueSearchResults = PagingData.empty(),
        selectedAppointments = emptySet()
    )
    verify(ui).hideProgress()
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search inputs are empty and has no results, then hide download and share buttons`() {
    // given
    val emptyOverdueSearchResults = PagingData.empty<OverdueAppointment>()
    val model = defaultModel
        .overdueSearchInputsChanged(emptyList())
        .overdueSearchResultsLoaded(emptyOverdueSearchResults)
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideDownloadAndShareButtons()
    verify(ui).hideSearchResults()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search inputs are not empty and has no results, then display no search results and hide download and share buttons`() {
    // given
    val emptyOverdueSearchResults = PagingData.empty<OverdueAppointment>()
    val model = defaultModel
        .overdueSearchInputsChanged(searchInputs = listOf("Ani"))
        .overdueSearchResultsLoaded(emptyOverdueSearchResults)
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setOverdueSearchResultsPagingData(
        overdueSearchResults = emptyOverdueSearchResults,
        selectedAppointments = emptySet()
    )
    verify(ui).hideSearchResults()
    verify(ui).showNoSearchResults()
    verify(ui).hideProgress()
    verify(ui).hideDownloadAndShareButtons()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when progress state is done, then render search results and show download and share buttons`() {
    // given
    val overdueSearchResults = PagingData.from(listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("463241b0-ccf3-464f-a1b4-636fcfdb0447"))
    ))
    val searchQuery = "Ani"
    val model = defaultModel
        .overdueSearchInputsChanged(listOf(searchQuery))
        .overdueSearchResultsLoaded(overdueSearchResults)
        .loadStateChanged(DONE)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setOverdueSearchResultsPagingData(
        overdueSearchResults = overdueSearchResults,
        selectedAppointments = emptySet()
    )
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verify(ui).showSearchResults()
    verify(ui).showDownloadAndShareButtons()
    verify(ui).hideSelectedOverdueAppointmentCount()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when overdue appointments are selected, then show overdue selected count`() {
    // given
    val overdueSearchResults = PagingData.from(listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("463241b0-ccf3-464f-a1b4-636fcfdb0447"))
    ))
    val selectedAppointments = setOf(UUID.fromString("4ab9f2ee-64a0-48c9-99c4-35f46c2e43a4"))
    val searchQuery = "Ani"
    val model = defaultModel
        .overdueSearchInputsChanged(listOf(searchQuery))
        .overdueSearchResultsLoaded(overdueSearchResults)
        .loadStateChanged(DONE)
        .selectedOverdueAppointmentsChanged(selectedAppointments)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setOverdueSearchResultsPagingData(
        overdueSearchResults = overdueSearchResults,
        selectedAppointments = selectedAppointments
    )
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
    val searchQuery = "Ani"
    val overdueSearchResults = PagingData.from(listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("463241b0-ccf3-464f-a1b4-636fcfdb0447"))
    ))
    val model = defaultModel
        .overdueSearchInputsChanged(listOf(searchQuery))
        .overdueSearchResultsLoaded(overdueSearchResults)
        .loadStateChanged(DONE)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setOverdueSearchResultsPagingData(
        overdueSearchResults = overdueSearchResults,
        selectedAppointments = emptySet()
    )
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
    verify(ui).hideDownloadAndShareButtons()
    verify(ui).hideSearchResults()
    verify(ui).hideNoSearchResults()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }
}
