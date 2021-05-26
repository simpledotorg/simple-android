package org.simple.clinic.summary.assignedfacility

import java.util.UUID

sealed class AssignedFacilityEffect

data class LoadAssignedFacility(val patientUuid: UUID) : AssignedFacilityEffect()

data class ChangeAssignedFacility(
    val patientUuid: UUID,
    val updatedAssignedFacilityId: UUID
) : AssignedFacilityEffect()

object OpenFacilitySelection : AssignedFacilityEffect()
