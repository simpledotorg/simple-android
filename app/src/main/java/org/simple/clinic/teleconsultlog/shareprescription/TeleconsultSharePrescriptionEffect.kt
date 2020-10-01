package org.simple.clinic.teleconsultlog.shareprescription

import java.util.UUID

sealed class TeleconsultSharePrescriptionEffect

data class LoadPatientDetails(val patientUuid: UUID) : TeleconsultSharePrescriptionEffect()

