package org.simple.clinic.reassignPatient

import java.util.UUID

sealed class ReassignPatientEffect

data class LoadAssignedFacility(val patientUuid: UUID) : ReassignPatientEffect()

