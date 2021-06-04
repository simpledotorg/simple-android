package org.simple.clinic.instantsearch

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.PatientSearchResultItemView
import java.util.UUID

class InstantSearchResultsItemTypeTest {

  private val currentFacilityUuid = UUID.fromString("69cf85c8-6788-4071-b985-0536ae606b70")
  private val otherFacilityUuid = UUID.fromString("28ed7dca-828e-4178-942f-fd6937f37155")
  private val currentFacility = TestData.facility(currentFacilityUuid)
  private val searchQueryReceived = "A"
  private val searchQueryEmpty = ""

  @Test
  fun `list items must be generated from the search results`() {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = TestData.patientSearchResult(patientUuid1, assignedFacilityId = currentFacilityUuid)
    val patientSearchResult2 = TestData.patientSearchResult(patientUuid2, assignedFacilityId = otherFacilityUuid)
    val searchResults = listOf(
        patientSearchResult1,
        patientSearchResult2
    )

    // when
    val listItems = InstantSearchResultsItemType.from(
        patientSearchResults = searchResults,
        currentFacility = currentFacility,
        searchQuery = searchQueryReceived
    )

    // then
    val expected = listOf(
        InstantSearchResultsItemType.AssignedFacilityHeader(facilityName = currentFacility.name),
        InstantSearchResultsItemType.SearchResult(
            searchResultViewModel = PatientSearchResultItemView.PatientSearchResultViewModel(
                uuid = patientSearchResult1.uuid,
                fullName = patientSearchResult1.fullName,
                gender = patientSearchResult1.gender,
                age = patientSearchResult1.age,
                dateOfBirth = patientSearchResult1.dateOfBirth,
                address = patientSearchResult1.address,
                phoneNumber = patientSearchResult1.phoneNumber,
                lastSeen = patientSearchResult1.lastSeen,
                identifier = patientSearchResult1.identifier
            ),
            currentFacilityId = currentFacilityUuid,
            searchQuery = searchQueryReceived
        ),
        InstantSearchResultsItemType.NearbyFacilitiesHeader,
        InstantSearchResultsItemType.SearchResult(
            searchResultViewModel = PatientSearchResultItemView.PatientSearchResultViewModel(
                uuid = patientSearchResult2.uuid,
                fullName = patientSearchResult2.fullName,
                gender = patientSearchResult2.gender,
                age = patientSearchResult2.age,
                dateOfBirth = patientSearchResult2.dateOfBirth,
                address = patientSearchResult2.address,
                phoneNumber = patientSearchResult2.phoneNumber,
                lastSeen = patientSearchResult2.lastSeen,
                identifier = patientSearchResult2.identifier
            ),
            currentFacilityId = currentFacilityUuid,
            searchQuery = searchQueryReceived
        )
    )
    assertThat(listItems).isEqualTo(expected)
  }

  @Test
  fun `when there are no search results in other facilities, the other results header must not be generated`() {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = TestData.patientSearchResult(patientUuid1, assignedFacilityId = currentFacilityUuid)
    val patientSearchResult2 = TestData.patientSearchResult(patientUuid2, assignedFacilityId = currentFacilityUuid)
    val searchResults = listOf(
        patientSearchResult1,
        patientSearchResult2
    )

    // when
    val listItems = InstantSearchResultsItemType.from(
        patientSearchResults = searchResults,
        currentFacility = currentFacility,
        searchQuery = searchQueryEmpty
    )

    // then
    val expected = listOf(
        InstantSearchResultsItemType.AssignedFacilityHeader(facilityName = currentFacility.name),
        InstantSearchResultsItemType.SearchResult(
            searchResultViewModel = PatientSearchResultItemView.PatientSearchResultViewModel(
                uuid = patientSearchResult1.uuid,
                fullName = patientSearchResult1.fullName,
                gender = patientSearchResult1.gender,
                age = patientSearchResult1.age,
                dateOfBirth = patientSearchResult1.dateOfBirth,
                address = patientSearchResult1.address,
                phoneNumber = patientSearchResult1.phoneNumber,
                lastSeen = patientSearchResult1.lastSeen,
                identifier = patientSearchResult1.identifier
            ),
            currentFacilityId = currentFacilityUuid,
            searchQuery = searchQueryEmpty
        ),
        InstantSearchResultsItemType.SearchResult(
            searchResultViewModel = PatientSearchResultItemView.PatientSearchResultViewModel(
                uuid = patientSearchResult2.uuid,
                fullName = patientSearchResult2.fullName,
                gender = patientSearchResult2.gender,
                age = patientSearchResult2.age,
                dateOfBirth = patientSearchResult2.dateOfBirth,
                address = patientSearchResult2.address,
                phoneNumber = patientSearchResult2.phoneNumber,
                lastSeen = patientSearchResult2.lastSeen,
                identifier = patientSearchResult2.identifier
            ),
            currentFacilityId = currentFacilityUuid,
            searchQuery = searchQueryEmpty
        )
    )
    assertThat(listItems).isEqualTo(expected)
  }

  @Test
  fun `when there are no search results in the current facility, then the nearby facilities should be shown`() {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = TestData.patientSearchResult(patientUuid1, assignedFacilityId = otherFacilityUuid)
    val patientSearchResult2 = TestData.patientSearchResult(patientUuid2, assignedFacilityId = otherFacilityUuid)
    val searchResults = listOf(
        patientSearchResult1,
        patientSearchResult2
    )

    // when
    val listItems = InstantSearchResultsItemType.from(
        patientSearchResults = searchResults,
        currentFacility = currentFacility,
        searchQuery = searchQueryEmpty
    )

    // then
    val expected = listOf(
        InstantSearchResultsItemType.NearbyFacilitiesHeader,
        InstantSearchResultsItemType.SearchResult(
            searchResultViewModel = PatientSearchResultItemView.PatientSearchResultViewModel(
                uuid = patientSearchResult1.uuid,
                fullName = patientSearchResult1.fullName,
                gender = patientSearchResult1.gender,
                age = patientSearchResult1.age,
                dateOfBirth = patientSearchResult1.dateOfBirth,
                address = patientSearchResult1.address,
                phoneNumber = patientSearchResult1.phoneNumber,
                lastSeen = patientSearchResult1.lastSeen,
                identifier = patientSearchResult1.identifier
            ),
            currentFacilityId = currentFacilityUuid,
            searchQuery = searchQueryEmpty

        ),
        InstantSearchResultsItemType.SearchResult(
            searchResultViewModel = PatientSearchResultItemView.PatientSearchResultViewModel(
                uuid = patientSearchResult2.uuid,
                fullName = patientSearchResult2.fullName,
                gender = patientSearchResult2.gender,
                age = patientSearchResult2.age,
                dateOfBirth = patientSearchResult2.dateOfBirth,
                address = patientSearchResult2.address,
                phoneNumber = patientSearchResult2.phoneNumber,
                lastSeen = patientSearchResult2.lastSeen,
                identifier = patientSearchResult2.identifier
            ),
            currentFacilityId = currentFacilityUuid,
            searchQuery = searchQueryEmpty
        )
    )
    assertThat(listItems).isEqualTo(expected)
  }

  @Test
  fun `when there are no search results, then no list items must be generated`() {
    // given
    val searchResults = emptyList<PatientSearchResult>()

    // when
    val listItems = InstantSearchResultsItemType.from(
        patientSearchResults = searchResults,
        currentFacility = currentFacility,
        searchQuery = searchQueryEmpty
    )

    // then
    assertThat(listItems).isEmpty()
  }
}
