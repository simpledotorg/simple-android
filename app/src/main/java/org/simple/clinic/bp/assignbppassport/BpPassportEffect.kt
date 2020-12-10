package org.simple.clinic.bp.assignbppassport

import org.simple.clinic.patient.OngoingNewPatientEntry

sealed class BpPassportEffect

data class SaveNewOngoingPatientEntry(val entry: OngoingNewPatientEntry) : BpPassportEffect()
