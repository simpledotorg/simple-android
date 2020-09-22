package org.simple.clinic.teleconsultlog.prescription

import java.util.UUID

sealed class TeleconsultPrescriptionEffect

data class LoadPatientDetails(val patientUuid: UUID) : TeleconsultPrescriptionEffect()

object GoBack : TeleconsultPrescriptionEffect()

object ShowSignatureRequiredError : TeleconsultPrescriptionEffect()
