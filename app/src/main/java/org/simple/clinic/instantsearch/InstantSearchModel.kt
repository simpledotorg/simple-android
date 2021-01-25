package org.simple.clinic.instantsearch

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier

@Parcelize
data class InstantSearchModel(
    val facility: Facility?,
    val searchQuery: String?,
    val additionalIdentifier: Identifier?,
    val instantSearchProgressState: InstantSearchProgressState?,
    val bpPassportSheetAlreadyOpened: Boolean
) : Parcelable {

  val hasFacility: Boolean
    get() = facility != null

  val hasSearchQuery: Boolean
    get() = !searchQuery.isNullOrBlank()

  val hasAdditionalIdentifier: Boolean
    get() = additionalIdentifier != null

  companion object {
    fun create(additionalIdentifier: Identifier?) = InstantSearchModel(
        facility = null,
        searchQuery = null,
        additionalIdentifier = additionalIdentifier,
        instantSearchProgressState = null,
        bpPassportSheetAlreadyOpened = false
    )
  }

  fun facilityLoaded(facility: Facility): InstantSearchModel {
    return copy(facility = facility)
  }

  fun searchQueryChanged(searchQuery: String): InstantSearchModel {
    return copy(searchQuery = searchQuery)
  }

  fun loadingAllPatients(): InstantSearchModel {
    return copy(instantSearchProgressState = InstantSearchProgressState.IN_PROGRESS)
  }

  fun allPatientsLoaded(): InstantSearchModel {
    return copy(instantSearchProgressState = InstantSearchProgressState.DONE)
  }

  fun loadingSearchResults(): InstantSearchModel {
    return copy(instantSearchProgressState = InstantSearchProgressState.IN_PROGRESS)
  }

  fun searchResultsLoaded(): InstantSearchModel {
    return copy(instantSearchProgressState = InstantSearchProgressState.DONE)
  }

  fun bpPassportSheetOpened(): InstantSearchModel {
    return copy(bpPassportSheetAlreadyOpened = true)
  }
}
