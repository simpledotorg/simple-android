package org.simple.clinic.instantsearch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshSharedHealthRecordId

@Parcelize
data class InstantSearchModel(
    val facility: Facility?,
    val searchQuery: String?,
    val additionalIdentifier: Identifier?,
    val scannedQrCodeSheetAlreadyOpened: Boolean,
    val instantSearchProgressState: InstantSearchProgressState?
) : Parcelable {

  val hasFacility: Boolean
    get() = facility != null

  val hasSearchQuery: Boolean
    get() = !searchQuery.isNullOrBlank()

  val hasAdditionalIdentifier: Boolean
    get() = additionalIdentifier != null

  val isAdditionalIdentifierBpPassport: Boolean
    get() = additionalIdentifier?.type == BpPassport && hasAdditionalIdentifier

  val isAdditionalIdentifierAnSHRID: Boolean
    get() = additionalIdentifier?.type == BangladeshSharedHealthRecordId && hasAdditionalIdentifier

  companion object {
    fun create(
        additionalIdentifier: Identifier?,
        searchQuery: String?
    ) = InstantSearchModel(
        facility = null,
        searchQuery = searchQuery,
        additionalIdentifier = additionalIdentifier,
        scannedQrCodeSheetAlreadyOpened = false,
        instantSearchProgressState = null
    )
  }

  fun facilityLoaded(facility: Facility): InstantSearchModel {
    return copy(facility = facility)
  }

  fun searchQueryChanged(searchQuery: String): InstantSearchModel {
    return copy(searchQuery = searchQuery)
  }

  fun scannedQrCodeSheetOpened(): InstantSearchModel {
    return copy(scannedQrCodeSheetAlreadyOpened = true)
  }

  fun additionalIdentifierUpdated(additionalIdentifier: Identifier): InstantSearchModel {
    return copy(additionalIdentifier = additionalIdentifier)
  }

  fun loadStateChanged(instantSearchProgressState: InstantSearchProgressState): InstantSearchModel {
    return copy(instantSearchProgressState = instantSearchProgressState)
  }
}
