package org.simple.clinic.teleconsultlog.shareprescription

import java.util.UUID

sealed class TeleconsultSharePrescriptionEffect

data class LoadPatientDetails(val patientUuid: UUID) : TeleconsultSharePrescriptionEffect()

data class LoadPatientMedicines(val patientUuid: UUID) : TeleconsultSharePrescriptionEffect()

object LoadSignature : TeleconsultSharePrescriptionEffect()
