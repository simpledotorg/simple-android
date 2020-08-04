package org.simple.clinic.search.results

import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class PatientSearchResultsEffect

data class OpenPatientSummary(val patientUuid: UUID) : PatientSearchResultsEffect()

data class OpenLinkIdWithPatientScreen(
    val patientUuid: UUID,
    val additionalIdentifier: Identifier
): PatientSearchResultsEffect()

data class SaveNewOngoingPatientEntry(val entry: OngoingNewPatientEntry): PatientSearchResultsEffect()

object OpenPatientEntryScreen: PatientSearchResultsEffect()
