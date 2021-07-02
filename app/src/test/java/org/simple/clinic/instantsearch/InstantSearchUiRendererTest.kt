package org.simple.clinic.instantsearch

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.instantsearch.InstantSearchProgressState.DONE
import org.simple.clinic.instantsearch.InstantSearchProgressState.IN_PROGRESS
import org.simple.clinic.instantsearch.InstantSearchProgressState.NO_RESULTS
import java.util.UUID

class InstantSearchUiRendererTest {

  private val defaultModel = InstantSearchModel.create(
      additionalIdentifier = null,
      patientPrefillInfo = null,
      searchQuery = null
  )

  private val ui = mock<InstantSearchUi>()
  private val uiRenderer = InstantSearchUiRenderer(ui)

  @Test
  fun `when progress state is in progress, then show progress indicator`() {
    // given
    val inProgressModel = defaultModel.loadStateChanged(IN_PROGRESS)

    // when
    uiRenderer.render(inProgressModel)

    // then
    verify(ui).hideProgress()
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when progress state is done, then update ui`() {
    // given
    val doneProgressModel = defaultModel.loadStateChanged(DONE)

    // when
    uiRenderer.render(doneProgressModel)

    // then
    verify(ui).showResults()
    verify(ui).hideProgress()
    verify(ui).hideNoSearchResults()
    verify(ui).hideNoPatientsInFacility()
  }

  @Test
  fun `when no results are found and search query is empty, then show no patients in facility`() {
    // given
    val facility = TestData.facility(uuid = UUID.fromString("901e32af-e402-4437-be2e-f0def72de776"),
        name = "PHC Obvious")
    val noResults = defaultModel
        .facilityLoaded(facility = facility)
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(noResults)

    // then
    verify(ui).hideResults()
    verify(ui).hideProgress()
    verify(ui).hideNoSearchResults()
    verify(ui).showNoPatientsInFacility("PHC Obvious")
  }

  @Test
  fun `when no results are found and search query is not empty, then show no search results`() {
    // given
    val facility = TestData.facility(uuid = UUID.fromString("901e32af-e402-4437-be2e-f0def72de776"),
        name = "PHC Obvious")
    val noResults = defaultModel
        .facilityLoaded(facility = facility)
        .searchQueryChanged(searchQuery = "Patient")
        .loadStateChanged(NO_RESULTS)

    // when
    uiRenderer.render(noResults)

    // then
    verify(ui).hideResults()
    verify(ui).hideProgress()
    verify(ui).hideNoPatientsInFacility()
    verify(ui).showNoSearchResults()
  }
}
