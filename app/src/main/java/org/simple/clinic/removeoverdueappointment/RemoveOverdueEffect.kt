package org.simple.clinic.removeoverdueappointment

import java.util.UUID

sealed class RemoveOverdueEffect

data class MarkPatientAsVisited(val appointmentUuid: UUID) : RemoveOverdueEffect()
