package org.simple.clinic.searchresultsview

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class SearchResultsItemTypeTest {

  private val currentFacilityUuid = UUID.fromString("69cf85c8-6788-4071-b985-0536ae606b70")
  private val currentFacility = PatientMocker.facility(currentFacilityUuid)

  @Test
  fun `list items must be generated from the search results`() {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = PatientMocker.patientSearchResult(patientUuid1)
    val patientSearchResult2 = PatientMocker.patientSearchResult(patientUuid2)
    val searchResults = PatientSearchResults(
        visitedCurrentFacility = listOf(patientSearchResult1),
        notVisitedCurrentFacility = listOf(patientSearchResult2)
    )

    // when
    val listItems = SearchResultsItemType.from(
        results = searchResults,
        currentFacility = currentFacility
    )

    // then
    val expected = listOf(
        SearchResultsItemType.InCurrentFacilityHeader(facilityName = currentFacility.name),
        SearchResultsItemType.SearchResultRow(
            searchResult = patientSearchResult1,
            currentFacilityUuid = currentFacilityUuid
        ),
        SearchResultsItemType.NotInCurrentFacilityHeader,
        SearchResultsItemType.SearchResultRow(
            searchResult = patientSearchResult2,
            currentFacilityUuid = currentFacilityUuid
        )
    )
    assertThat(listItems).isEqualTo(expected)
  }

  @Test
  fun `when there are no search results in other facilities, the other results header must not be generated`() {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = PatientMocker.patientSearchResult(patientUuid1)
    val patientSearchResult2 = PatientMocker.patientSearchResult(patientUuid2)
    val searchResults = PatientSearchResults(
        visitedCurrentFacility = listOf(patientSearchResult1, patientSearchResult2),
        notVisitedCurrentFacility = emptyList()
    )

    // when
    val listItems = SearchResultsItemType.from(
        results = searchResults,
        currentFacility = currentFacility
    )

    // then
    val expected = listOf(
        SearchResultsItemType.InCurrentFacilityHeader(facilityName = currentFacility.name),
        SearchResultsItemType.SearchResultRow(
            searchResult = patientSearchResult1,
            currentFacilityUuid = currentFacilityUuid
        ),
        SearchResultsItemType.SearchResultRow(
            searchResult = patientSearchResult2,
            currentFacilityUuid = currentFacilityUuid
        )
    )
    assertThat(listItems).isEqualTo(expected)
  }

  @Test
  fun `when there are no search results in the current facility, then the current facility header with no results must be shown`() {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = PatientMocker.patientSearchResult(patientUuid1)
    val patientSearchResult2 = PatientMocker.patientSearchResult(patientUuid2)
    val searchResults = PatientSearchResults(
        visitedCurrentFacility = emptyList(),
        notVisitedCurrentFacility = listOf(patientSearchResult1, patientSearchResult2)
    )

    // when
    val listItems = SearchResultsItemType.from(
        results = searchResults,
        currentFacility = currentFacility
    )

    // then
    val expected = listOf(
        SearchResultsItemType.InCurrentFacilityHeader(facilityName = currentFacility.name),
        SearchResultsItemType.NoPatientsInCurrentFacility,
        SearchResultsItemType.NotInCurrentFacilityHeader,
        SearchResultsItemType.SearchResultRow(
            searchResult = patientSearchResult1,
            currentFacilityUuid = currentFacilityUuid
        ),
        SearchResultsItemType.SearchResultRow(
            searchResult = patientSearchResult2,
            currentFacilityUuid = currentFacilityUuid
        )
    )
    assertThat(listItems).isEqualTo(expected)
  }

  @Test
  fun `when there are no search results, then no list items must be generated`() {
    // given
    val searchResults = PatientSearchResults(
        visitedCurrentFacility = emptyList(),
        notVisitedCurrentFacility = emptyList()
    )

    // when
    val listItems = SearchResultsItemType.from(searchResults, currentFacility)

    // then
    assertThat(listItems).isEmpty()
  }
}
