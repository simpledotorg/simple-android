package org.simple.clinic.reassignPatient

import java.util.UUID

sealed class ReassignPatientEffect

data class LoadAssignedFacility(val patientUuid: UUID) : ReassignPatientEffect()

sealed class ReassignPatientViewEffect : ReassignPatientEffect()

data object CloseSheet : ReassignPatientViewEffect()

data object OpenSelectFacilitySheet : ReassignPatientViewEffect()
