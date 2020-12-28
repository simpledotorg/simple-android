package org.simple.clinic.searchresultsview

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.searchresultsview.SearchResultsItemType.InCurrentFacilityHeader
import org.simple.clinic.searchresultsview.SearchResultsItemType.NoPatientsInCurrentFacility
import org.simple.clinic.searchresultsview.SearchResultsItemType.NotInCurrentFacilityHeader
import org.simple.clinic.searchresultsview.SearchResultsItemType.SearchResultRow
import org.simple.clinic.widgets.PatientSearchResultItemView_Old.PatientSearchResultViewModel
import java.util.UUID

class SearchResultsItemTypeTest {

  private val currentFacilityUuid = UUID.fromString("69cf85c8-6788-4071-b985-0536ae606b70")
  private val currentFacility = TestData.facility(currentFacilityUuid)

  @Test
  fun `list items must be generated from the search results`() {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = TestData.patientSearchResult(patientUuid1)
    val patientSearchResult2 = TestData.patientSearchResult(patientUuid2)
    val searchResults = PatientSearchResults(
        visitedCurrentFacility = listOf(patientSearchResult1),
        notVisitedCurrentFacility = listOf(patientSearchResult2),
        currentFacility = currentFacility
    )

    // when
    val listItems = SearchResultsItemType.from(
        results = searchResults
    )

    // then
    val expected = listOf(
        InCurrentFacilityHeader(facilityName = currentFacility.name),
        SearchResultRow(
            searchResultViewModel = PatientSearchResultViewModel(
                uuid = patientSearchResult1.uuid,
                fullName = patientSearchResult1.fullName,
                gender = patientSearchResult1.gender,
                age = patientSearchResult1.age,
                dateOfBirth = patientSearchResult1.dateOfBirth,
                address = patientSearchResult1.address,
                phoneNumber = patientSearchResult1.phoneNumber,
                lastSeen = patientSearchResult1.lastSeen
            ),
            currentFacilityUuid = currentFacilityUuid
        ),
        NotInCurrentFacilityHeader,
        SearchResultRow(
            searchResultViewModel = PatientSearchResultViewModel(
                uuid = patientSearchResult2.uuid,
                fullName = patientSearchResult2.fullName,
                gender = patientSearchResult2.gender,
                age = patientSearchResult2.age,
                dateOfBirth = patientSearchResult2.dateOfBirth,
                address = patientSearchResult2.address,
                phoneNumber = patientSearchResult2.phoneNumber,
                lastSeen = patientSearchResult2.lastSeen
            ),
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
    val patientSearchResult1 = TestData.patientSearchResult(patientUuid1)
    val patientSearchResult2 = TestData.patientSearchResult(patientUuid2)
    val searchResults = PatientSearchResults(
        visitedCurrentFacility = listOf(patientSearchResult1, patientSearchResult2),
        notVisitedCurrentFacility = emptyList(),
        currentFacility = currentFacility
    )

    // when
    val listItems = SearchResultsItemType.from(
        results = searchResults
    )

    // then
    val expected = listOf(
        InCurrentFacilityHeader(facilityName = currentFacility.name),
        SearchResultRow(
            searchResultViewModel = PatientSearchResultViewModel(
                uuid = patientSearchResult1.uuid,
                fullName = patientSearchResult1.fullName,
                gender = patientSearchResult1.gender,
                age = patientSearchResult1.age,
                dateOfBirth = patientSearchResult1.dateOfBirth,
                address = patientSearchResult1.address,
                phoneNumber = patientSearchResult1.phoneNumber,
                lastSeen = patientSearchResult1.lastSeen
            ),
            currentFacilityUuid = currentFacilityUuid
        ),
        SearchResultRow(searchResultViewModel = PatientSearchResultViewModel(
            uuid = patientSearchResult2.uuid,
            fullName = patientSearchResult2.fullName,
            gender = patientSearchResult2.gender,
            age = patientSearchResult2.age,
            dateOfBirth = patientSearchResult2.dateOfBirth,
            address = patientSearchResult2.address,
            phoneNumber = patientSearchResult2.phoneNumber,
            lastSeen = patientSearchResult2.lastSeen
        ), currentFacilityUuid = currentFacilityUuid)
    )
    assertThat(listItems).isEqualTo(expected)
  }

  @Test
  fun `when there are no search results in the current facility, then the current facility header with no results must be shown`() {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = TestData.patientSearchResult(patientUuid1)
    val patientSearchResult2 = TestData.patientSearchResult(patientUuid2)
    val searchResults = PatientSearchResults(
        visitedCurrentFacility = emptyList(),
        notVisitedCurrentFacility = listOf(patientSearchResult1, patientSearchResult2),
        currentFacility = currentFacility
    )

    // when
    val listItems = SearchResultsItemType.from(
        results = searchResults
    )

    // then
    val expected = listOf(
        InCurrentFacilityHeader(facilityName = currentFacility.name),
        NoPatientsInCurrentFacility,
        NotInCurrentFacilityHeader,
        SearchResultRow(
            searchResultViewModel = PatientSearchResultViewModel(
                uuid = patientSearchResult1.uuid,
                fullName = patientSearchResult1.fullName,
                gender = patientSearchResult1.gender,
                age = patientSearchResult1.age,
                dateOfBirth = patientSearchResult1.dateOfBirth,
                address = patientSearchResult1.address,
                phoneNumber = patientSearchResult1.phoneNumber,
                lastSeen = patientSearchResult1.lastSeen
            ),
            currentFacilityUuid = currentFacilityUuid
        ),
        SearchResultRow(
            searchResultViewModel = PatientSearchResultViewModel(
                uuid = patientSearchResult2.uuid,
                fullName = patientSearchResult2.fullName,
                gender = patientSearchResult2.gender,
                age = patientSearchResult2.age,
                dateOfBirth = patientSearchResult2.dateOfBirth,
                address = patientSearchResult2.address,
                phoneNumber = patientSearchResult2.phoneNumber,
                lastSeen = patientSearchResult2.lastSeen
            ),
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
        notVisitedCurrentFacility = emptyList(),
        currentFacility = currentFacility
    )

    // when
    val listItems = SearchResultsItemType.from(searchResults)

    // then
    assertThat(listItems).isEmpty()
  }
}
