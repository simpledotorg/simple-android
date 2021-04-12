package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.overdue.AppointmentCancelReason
import java.util.UUID

sealed class RemoveOverdueEffect

data class MarkPatientAsVisited(val appointmentUuid: UUID) : RemoveOverdueEffect()

data class MarkPatientAsDead(val patientId: UUID, val appointmentId: UUID) : RemoveOverdueEffect()

data class CancelAppointment(
    val appointmentUuid: UUID,
    val reason: AppointmentCancelReason
) : RemoveOverdueEffect()

data class MarkPatientAsMovedToPrivate(val patientId: UUID) : RemoveOverdueEffect()
