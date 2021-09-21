package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import javax.inject.Inject

class CancelAppointmentWithReason  @Inject constructor(
    private val appointmentRepository: AppointmentRepository
){

  fun execute(
      appointment: Appointment,
      reason: AppointmentCancelReason
  ) {
    appointmentRepository.cancelWithReason(appointment.uuid, reason)
  }
}
