package org.simple.clinic.patientcontact

import java.util.UUID

sealed class PatientContactEffect

data class LoadPatient(val patientUuid: UUID): PatientContactEffect()
