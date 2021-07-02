package org.simple.clinic.instantsearch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientPrefillInfo
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId

@Parcelize
data class InstantSearchModel(
    val facility: Facility?,
    val searchQuery: String?,
    val additionalIdentifier: Identifier?,
    val scannedQrCodeSheetAlreadyOpened: Boolean,
    val patientPrefillInfo: PatientPrefillInfo?,
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

  val isAdditionalIdentifierAnNHID: Boolean
    get() = additionalIdentifier?.type == IndiaNationalHealthId && hasAdditionalIdentifier

  val canBePrefilled: Boolean
    get() = patientPrefillInfo != null && isAdditionalIdentifierAnNHID

  companion object {
    fun create(
        additionalIdentifier: Identifier?,
        patientPrefillInfo: PatientPrefillInfo?,
        searchQuery: String?
    ) = InstantSearchModel(
        facility = null,
        searchQuery = searchQuery,
        additionalIdentifier = additionalIdentifier,
        scannedQrCodeSheetAlreadyOpened = false,
        patientPrefillInfo = patientPrefillInfo,
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

  fun patientPrefillInfoUpdated(patientPrefillInfo: PatientPrefillInfo): InstantSearchModel {
    return copy(patientPrefillInfo = patientPrefillInfo)
  }

  fun loadStateChanged(instantSearchProgressState: InstantSearchProgressState): InstantSearchModel {
    return copy(instantSearchProgressState = instantSearchProgressState)
  }
}
