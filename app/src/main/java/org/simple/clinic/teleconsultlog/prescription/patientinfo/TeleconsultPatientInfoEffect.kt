package org.simple.clinic.teleconsultlog.prescription.patientinfo

import java.util.UUID

sealed class TeleconsultPatientInfoEffect

data class LoadPatientProfile(val patientUuid: UUID) : TeleconsultPatientInfoEffect()
