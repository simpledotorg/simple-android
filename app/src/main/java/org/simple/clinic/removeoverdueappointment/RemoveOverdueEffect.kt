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

data class MarkPatientAsRefusedToComeBack(val patientId: UUID) : RemoveOverdueEffect()

data object GoBack : RemoveOverdueEffect()

sealed class RemoveOverdueViewEffect : RemoveOverdueEffect()

data object GoBackAfterAppointmentRemoval : RemoveOverdueViewEffect()
