package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import java.util.UUID

sealed class RemoveOverdueEffect

data class MarkPatientAsVisited(val appointmentUuid: UUID) : RemoveOverdueEffect()

data class MarkPatientAsDead(val patientId: UUID, val appointmentId: UUID) : RemoveOverdueEffect()

data class CancelAppointment(
    val appointment: Appointment,
    val reason: AppointmentCancelReason
) : RemoveOverdueEffect()

data class MarkPatientAsMovedToPrivate(val patientId: UUID) : RemoveOverdueEffect()

data class MarkPatientAsTransferredToAnotherFacility(val patientId: UUID) : RemoveOverdueEffect()

object GoBack : RemoveOverdueEffect()

sealed class RemoveOverdueViewEffect : RemoveOverdueEffect()

object GoBackAfterAppointmentRemoval : RemoveOverdueViewEffect()
