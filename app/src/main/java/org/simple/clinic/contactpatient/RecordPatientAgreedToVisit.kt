package org.simple.clinic.contactpatient

import dagger.Lazy
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.CallResultRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.User
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.uuid.UuidGenerator
import javax.inject.Inject

class RecordPatientAgreedToVisit @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val callResultRepository: CallResultRepository,
    private val userClock: UserClock,
    private val utcClock: UtcClock,
    private val uuidGenerator: UuidGenerator,
    private val currentUser: Lazy<User>
) {

  fun execute(appointment: Appointment) {
    appointmentRepository.markAsAgreedToVisit(appointment.uuid, userClock)
    markAgreedToVisitCallOutcome(appointment)
  }

  private fun markAgreedToVisitCallOutcome(appointment: Appointment) {
    val callResult = CallResult.agreedToVisit(
        id = uuidGenerator.v4(),
        appointment = appointment,
        user = currentUser.get(),
        clock = utcClock,
        syncStatus = SyncStatus.PENDING
    )
    callResultRepository.save(listOf(callResult))
  }
}
