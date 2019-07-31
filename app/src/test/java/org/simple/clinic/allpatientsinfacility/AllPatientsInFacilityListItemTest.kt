package org.simple.clinic.allpatientsinfacility

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.FacilityHeader
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.SearchResult
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class AllPatientsInFacilityListItemTest {

  @Test
  fun `search results must be mapped to list items correctly`() {
    // given
    val facility = PatientMocker.facility(uuid = UUID.fromString("887c5fd8-e2bf-4ae0-bdc8-8ade18a7898a"), name = "Test Facility")
    val patientSearchResults = listOf(
        PatientMocker.patientSearchResult(uuid = UUID.fromString("a8b66bb7-231f-45af-9216-866e2ef0eae8")),
        PatientMocker.patientSearchResult(uuid = UUID.fromString("d80d26e8-5a16-4397-94b7-acce4a65f63c")),
        PatientMocker.patientSearchResult(uuid = UUID.fromString("ed6ce886-cb27-4574-93c9-a4fc133ff734"))
    ).map(::PatientSearchResultUiState)

    // when
    val listItems = AllPatientsInFacilityListItem.mapSearchResultsToListItems(
        FacilityUiState(facility.uuid, facility.name),
        patientSearchResults
    )

    // then
    val expectedListItems = listOf(
        FacilityHeader("Test Facility"),
        SearchResult(facility.uuid, patientSearchResults[0]),
        SearchResult(facility.uuid, patientSearchResults[1]),
        SearchResult(facility.uuid, patientSearchResults[2])
    )
    assertThat(listItems).isEqualTo(expectedListItems)
  }
}
