package org.simple.clinic.reassignPatient

import java.util.UUID

sealed class ReassignPatientEffect

data class LoadAssignedFacility(val patientUuid: UUID) : ReassignPatientEffect()

data class ChangeAssignedFacility(
    val patientUuid: UUID,
    val updatedAssignedFacilityId: UUID
) : ReassignPatientEffect()

sealed class ReassignPatientViewEffect : ReassignPatientEffect()

data object CloseSheet : ReassignPatientViewEffect()

data object OpenSelectFacilitySheet : ReassignPatientViewEffect()
