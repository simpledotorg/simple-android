package org.simple.clinic.home.overdue.search

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS
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
    verify(ui).hideProgress()
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search inputs are empty and has no results, then hide download and share buttons`() {
    // given
    val model = defaultModel
        .overdueSearchInputsChanged(emptyList())
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
    val model = defaultModel
        .overdueSearchInputsChanged(searchInputs = listOf("Ani"))
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideSearchResults()
    verify(ui).showNoSearchResults()
    verify(ui).hideProgress()
    verify(ui).hideDownloadAndShareButtons()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when progress state is done, then render search results and show download and share buttons`() {
    // given
    val selectedAppointments = setOf(UUID.fromString("d8924174-6109-4695-87a1-2c19a929eeb0"))
    val searchQuery = "Ani"
    val model = defaultModel
        .overdueSearchInputsChanged(listOf(searchQuery))
        .loadStateChanged(DONE)
        .selectedOverdueAppointmentsChanged(selectedAppointments)

    // when
    uiRenderer.render(model)

    // then
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
    val selectedAppointments = setOf(UUID.fromString("4ab9f2ee-64a0-48c9-99c4-35f46c2e43a4"))
    val searchQuery = "Ani"
    val model = defaultModel
        .overdueSearchInputsChanged(listOf(searchQuery))
        .loadStateChanged(DONE)
        .selectedOverdueAppointmentsChanged(selectedAppointments)

    // when
    uiRenderer.render(model)

    // then
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
    val model = defaultModel
        .overdueSearchInputsChanged(listOf(searchQuery))
        .loadStateChanged(DONE)

    // when
    uiRenderer.render(model)

    // then
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
