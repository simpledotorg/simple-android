package org.simple.clinic.contactpatient

import dagger.Lazy
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.CallResultRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.uuid.UuidGenerator
import java.time.LocalDate
import javax.inject.Inject

class CreateReminderForAppointment @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val callResultRepository: CallResultRepository,
    private val utcClock: UtcClock,
    private val currentUser: Lazy<User>,
    private val uuidGenerator: UuidGenerator,
) {

  fun execute(
      appointment: Appointment,
      reminderDate: LocalDate
  ) {
    appointmentRepository.createReminder(appointment.uuid, reminderDate)
    markRemindToCallLaterOutcomeForAppointment(appointment)
  }

  private fun markRemindToCallLaterOutcomeForAppointment(appointment: Appointment) {
    val callResult = CallResult.remindToCallLater(
        id = uuidGenerator.v4(),
        appointment = appointment,
        user = currentUser.get(),
        clock = utcClock,
        syncStatus = SyncStatus.PENDING
    )

    callResultRepository.save(listOf(callResult))
  }
}
