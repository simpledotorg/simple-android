package org.simple.clinic.instantsearch

import androidx.paging.PagingData
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class InstantSearchEffect

object LoadCurrentFacility : InstantSearchEffect()

data class LoadAllPatients(val facility: Facility) : InstantSearchEffect()

data class SearchWithCriteria(
    val criteria: PatientSearchCriteria,
    val facility: Facility
) : InstantSearchEffect()

data class ValidateSearchQuery(val searchQuery: String) : InstantSearchEffect()

data class SaveNewOngoingPatientEntry(val ongoingNewPatientEntry: OngoingNewPatientEntry) : InstantSearchEffect()

data class CheckIfPatientAlreadyHasAnExistingNHID(val patientId: UUID) : InstantSearchEffect()

sealed class InstantSearchViewEffect : InstantSearchEffect()

data class ShowAllPatients(
    val patients: PagingData<PatientSearchResult>,
    val facility: Facility
) : InstantSearchViewEffect()

data class ShowPatientSearchResults(
    val patients: PagingData<PatientSearchResult>,
    val facility: Facility,
    val searchQuery: String
) : InstantSearchViewEffect()

data class OpenPatientSummary(val patientId: UUID) : InstantSearchViewEffect()

data class OpenLinkIdWithPatientScreen(
    val patientId: UUID,
    val identifier: Identifier
) : InstantSearchViewEffect()

data class OpenScannedQrCodeSheet(val identifier: Identifier) : InstantSearchViewEffect()

data class OpenPatientEntryScreen(val facility: Facility) : InstantSearchViewEffect()

object ShowKeyboard : InstantSearchViewEffect()

object OpenQrCodeScanner : InstantSearchViewEffect()

object ShowNHIDErrorDialog : InstantSearchViewEffect()

data class PrefillSearchQuery(val searchQuery: String) : InstantSearchViewEffect()
