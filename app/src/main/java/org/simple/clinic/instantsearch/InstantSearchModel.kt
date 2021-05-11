package org.simple.clinic.instantsearch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId

@Parcelize
data class InstantSearchModel(
    val facility: Facility?,
    val searchQuery: String?,
    val additionalIdentifier: Identifier?,
    val instantSearchProgressState: InstantSearchProgressState?,
    val scannedQrCodeSheetAlreadyOpened: Boolean
) : Parcelable {

  val hasFacility: Boolean
    get() = facility != null

  val hasSearchQuery: Boolean
    get() = !searchQuery.isNullOrBlank()

  val hasAdditionalIdentifier: Boolean
    get() = additionalIdentifier != null

  val isAdditionalIdentifierAnNHID: Boolean
    get() = additionalIdentifier?.type == IndiaNationalHealthId && hasAdditionalIdentifier

  companion object {
    fun create(additionalIdentifier: Identifier?) = InstantSearchModel(
        facility = null,
        searchQuery = null,
        additionalIdentifier = additionalIdentifier,
        instantSearchProgressState = null,
        scannedQrCodeSheetAlreadyOpened = false
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

  fun scannedQrCodeSheetOpened(): InstantSearchModel {
    return copy(scannedQrCodeSheetAlreadyOpened = true)
  }

  fun additionalIdentifierUpdated(additionalIdentifier: Identifier): InstantSearchModel {
    return copy(additionalIdentifier = additionalIdentifier)
  }
}
