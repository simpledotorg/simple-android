package org.simple.clinic.removeoverdueappointment

import dagger.Lazy
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.CallResultRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.uuid.UuidGenerator
import javax.inject.Inject

class CancelAppointmentWithReason  @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val callResultRepository: CallResultRepository,
    private val uuidGenerator: UuidGenerator,
    private val utcClock: UtcClock,
    private val currentUser: Lazy<User>
){

  fun execute(
      appointment: Appointment,
      reason: AppointmentCancelReason
  ) {
    appointmentRepository.cancelWithReason(appointment.uuid, reason)
    recordCallResultForRemovedAppointment(appointment, reason)
  }

  private fun recordCallResultForRemovedAppointment(
      appointment: Appointment,
      reason: AppointmentCancelReason
  ) {
    val callResult = CallResult.removed(
        id = uuidGenerator.v4(),
        appointment = appointment,
        removeReason = reason,
        user = currentUser.get(),
        clock = utcClock,
        syncStatus = SyncStatus.PENDING
    )
    callResultRepository.save(listOf(callResult))
  }
}
