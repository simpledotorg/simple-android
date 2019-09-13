package org.simple.clinic.allpatientsinfacility

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class AllPatientsInFacilityUiRendererTest {
  private val defaultModel = AllPatientsInFacilityModel.FETCHING_PATIENTS
  private val ui = mock<AllPatientsInFacilityUi>()
  private val uiRenderer = AllPatientsInFacilityUiRenderer(ui)
  private val facility = PatientMocker.facility(UUID.fromString("1be5097b-1c9f-4f78-aa70-9b907f241669"))

  @Test
  fun `when a facility is being fetched, then do nothing`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when a facility is fetched, then do nothing`() {
    // given
    val fetchingPatientsState = defaultModel
        .facilityFetched(facility)

    // when
    uiRenderer.render(fetchingPatientsState)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when a facility has no patients, then show no patients found in facility`() {
    // given
    val noPatientsFoundInFacilityState = defaultModel
        .facilityFetched(facility)
        .noPatients()

    // when
    uiRenderer.render(noPatientsFoundInFacilityState)

    // then
    verify(ui).showNoPatientsFound(noPatientsFoundInFacilityState.facilityUiState!!.name)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when a facility has patients, then show the patients search result list`() {
    // given
    val patientSearchResults = listOf(PatientMocker.patientSearchResult())
        .map(::PatientSearchResultUiState)

    val hasPatientsInFacilityState = defaultModel
        .facilityFetched(facility)
        .hasPatients(patientSearchResults)

    // when
    uiRenderer.render(hasPatientsInFacilityState)

    // then
    verify(ui).showPatients(FacilityUiState(facility.uuid, facility.name), patientSearchResults)
    verifyNoMoreInteractions(ui)
  }
}
