package org.simple.clinic.bp.assignbppassport

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.OngoingNewPatientEntry

sealed class BpPassportEffect

data class SaveNewOngoingPatientEntry(val entry: OngoingNewPatientEntry) : BpPassportEffect()

object FetchCurrentFacility : BpPassportEffect()

data class OpenPatientEntryScreen(val facility: Facility) : BpPassportEffect()

object CloseSheet : BpPassportEffect()

data class SendBlankBpPassportResult(val bpPassportResult: BlankBpPassportResult) : BpPassportEffect()
