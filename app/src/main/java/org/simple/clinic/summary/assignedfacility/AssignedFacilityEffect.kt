package org.simple.clinic.summary.assignedfacility

import java.util.UUID

sealed class AssignedFacilityEffect

data class LoadAssignedFacility(val patientUuid: UUID) : AssignedFacilityEffect()
