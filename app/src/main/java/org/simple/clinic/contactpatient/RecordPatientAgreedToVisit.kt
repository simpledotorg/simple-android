package org.simple.clinic.contactpatient

import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.UserClock
import javax.inject.Inject

class RecordPatientAgreedToVisit @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val userClock: UserClock
) {

  fun execute(appointment: Appointment) {
    appointmentRepository.markAsAgreedToVisit(appointment.uuid, userClock)
  }
}
