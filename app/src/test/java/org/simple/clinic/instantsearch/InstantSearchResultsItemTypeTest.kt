package org.simple.clinic.instantsearch

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.instantsearch.InstantSearchResultsItemType.AssignedFacilityHeader
import org.simple.clinic.instantsearch.InstantSearchResultsItemType.NearbyFacilitiesHeader
import org.simple.clinic.instantsearch.InstantSearchResultsItemType.SearchResult
import java.util.UUID

class InstantSearchResultsItemTypeTest {

  private val currentFacility = TestData.facility(
      uuid = UUID.fromString("7174598a-434f-4043-9140-8c23046d4ff6")
  )
  private val otherFacility = TestData.facility(
      uuid = UUID.fromString("c7328023-bcd4-4862-b16f-5404df3fd051")
  )

  @Test
  fun assigned_facility_header_must_be_shown_when_the_first_search_result_is_at_current_facility() {
    // given
    val patientInCurrentFacility = SearchResult.forSearchResult(
        searchResult = TestData.patientSearchResult(
            uuid = UUID.fromString("2ea03550-896d-4a3d-a977-5ec9c3f91002"),
            assignedFacilityId = currentFacility.uuid
        ),
        currentFacilityId = currentFacility.uuid,
        searchQuery = null
    )

    // when
    val header = InstantSearchResultsItemType.insertHeaders(
        beforeSearchResult = null,
        afterSearchResult = patientInCurrentFacility,
        currentFacility = currentFacility
    )

    // then
    assertThat(header).isEqualTo(AssignedFacilityHeader(currentFacility.name))
  }

  @Test
  fun nearby_facility_header_must_be_shown_when_the_first_search_results_is_not_at_current_facility() {
    // given
    val patientInOtherFacility = SearchResult.forSearchResult(
        searchResult = TestData.patientSearchResult(
            uuid = UUID.fromString("2ea03550-896d-4a3d-a977-5ec9c3f91002"),
            assignedFacilityId = otherFacility.uuid
        ),
        currentFacilityId = currentFacility.uuid,
        searchQuery = null
    )

    // when
    val header = InstantSearchResultsItemType.insertHeaders(
        beforeSearchResult = null,
        afterSearchResult = patientInOtherFacility,
        currentFacility = currentFacility
    )

    // then
    assertThat(header).isEqualTo(NearbyFacilitiesHeader)
  }

  @Test
  fun nearby_facility_header_must_be_shown_between_search_results_at_current_facility_and_not_at_current_facility() {
    // given
    val patientInCurrentFacility = SearchResult.forSearchResult(
        searchResult = TestData.patientSearchResult(
            uuid = UUID.fromString("2ea03550-896d-4a3d-a977-5ec9c3f91002"),
            assignedFacilityId = currentFacility.uuid
        ),
        currentFacilityId = currentFacility.uuid,
        searchQuery = null
    )

    val patientInOtherFacility = SearchResult.forSearchResult(
        searchResult = TestData.patientSearchResult(
            uuid = UUID.fromString("39d44632-3183-4755-ae10-966651447197"),
            assignedFacilityId = otherFacility.uuid
        ),
        currentFacilityId = currentFacility.uuid,
        searchQuery = null
    )

    // when
    val header = InstantSearchResultsItemType.insertHeaders(
        beforeSearchResult = patientInCurrentFacility,
        afterSearchResult = patientInOtherFacility,
        currentFacility = currentFacility
    )

    // then
    assertThat(header).isEqualTo(NearbyFacilitiesHeader)
  }

  @Test
  fun no_header_should_be_shown_when_both_search_results_are_at_current_facility() {
    // given
    val patient1InCurrentFacility = SearchResult.forSearchResult(
        searchResult = TestData.patientSearchResult(
            uuid = UUID.fromString("2ea03550-896d-4a3d-a977-5ec9c3f91002"),
            assignedFacilityId = currentFacility.uuid
        ),
        currentFacilityId = currentFacility.uuid,
        searchQuery = null
    )

    val patient2InCurrentFacility = SearchResult.forSearchResult(
        searchResult = TestData.patientSearchResult(
            uuid = UUID.fromString("39d44632-3183-4755-ae10-966651447197"),
            assignedFacilityId = currentFacility.uuid
        ),
        currentFacilityId = currentFacility.uuid,
        searchQuery = null
    )

    // when
    val header = InstantSearchResultsItemType.insertHeaders(
        beforeSearchResult = patient1InCurrentFacility,
        afterSearchResult = patient2InCurrentFacility,
        currentFacility = currentFacility
    )

    // then
    assertThat(header).isNull()
  }

  @Test
  fun no_header_should_be_shown_when_both_search_results_are_not_at_current_facility() {
    // given
    val patient1InOtherFacility = SearchResult.forSearchResult(
        searchResult = TestData.patientSearchResult(
            uuid = UUID.fromString("2ea03550-896d-4a3d-a977-5ec9c3f91002"),
            assignedFacilityId = otherFacility.uuid
        ),
        currentFacilityId = currentFacility.uuid,
        searchQuery = null
    )

    val patient2InOtherFacility = SearchResult.forSearchResult(
        searchResult = TestData.patientSearchResult(
            uuid = UUID.fromString("39d44632-3183-4755-ae10-966651447197"),
            assignedFacilityId = otherFacility.uuid
        ),
        currentFacilityId = currentFacility.uuid,
        searchQuery = null
    )

    // when
    val header = InstantSearchResultsItemType.insertHeaders(
        beforeSearchResult = patient1InOtherFacility,
        afterSearchResult = patient2InOtherFacility,
        currentFacility = currentFacility
    )

    // then
    assertThat(header).isNull()
  }
}
