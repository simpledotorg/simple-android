package org.simple.clinic.reassignpatient

import java.util.UUID

sealed class ReassignPatientEffect

data class LoadAssignedFacility(val patientUuid: UUID) : ReassignPatientEffect()

data class ChangeAssignedFacility(
    val patientUuid: UUID,
    val updatedAssignedFacilityId: UUID
) : ReassignPatientEffect()

sealed class ReassignPatientViewEffect : ReassignPatientEffect()

data class CloseSheet(val sheetClosedFrom: ReassignPatientSheetClosedFrom) : ReassignPatientViewEffect()

data object OpenSelectFacilitySheet : ReassignPatientViewEffect()
