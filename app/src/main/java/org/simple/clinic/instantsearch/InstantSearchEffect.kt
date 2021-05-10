package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class InstantSearchEffect

object LoadCurrentFacility : InstantSearchEffect()

data class LoadAllPatients(val facility: Facility) : InstantSearchEffect()

data class SearchWithCriteria(val criteria: PatientSearchCriteria, val facility: Facility) : InstantSearchEffect()

data class ShowAllPatients(val patients: List<PatientSearchResult>, val facility: Facility) : InstantSearchEffect()

data class ShowPatientSearchResults(val patients: List<PatientSearchResult>, val facility: Facility, val searchQuery: String) : InstantSearchEffect()

data class ValidateSearchQuery(val searchQuery: String) : InstantSearchEffect()

data class OpenPatientSummary(val patientId: UUID) : InstantSearchEffect()

data class OpenLinkIdWithPatientScreen(val patientId: UUID, val identifier: Identifier) : InstantSearchEffect()

data class OpenScannedQrCodeSheet(val identifier: Identifier) : InstantSearchEffect()

data class ShowNoPatientsInFacility(val facility: Facility) : InstantSearchEffect()

object ShowNoSearchResults : InstantSearchEffect()

object HideNoPatientsInFacility : InstantSearchEffect()

object HideNoSearchResults : InstantSearchEffect()

data class SaveNewOngoingPatientEntry(val ongoingNewPatientEntry: OngoingNewPatientEntry) : InstantSearchEffect()

data class OpenPatientEntryScreen(val facility: Facility) : InstantSearchEffect()

object ShowKeyboard : InstantSearchEffect()

object OpenQrCodeScanner : InstantSearchEffect()

data class CheckIfPatientAlreadyHasAnExistingNHID(val patientId: UUID) : InstantSearchEffect()

object ShowNHIDErrorDialog : InstantSearchEffect()
