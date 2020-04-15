package org.simple.clinic.overdue

import org.simple.clinic.util.UserClock
import org.simple.clinic.util.plus
import org.threeten.bp.LocalDate

data class PotentialAppointmentDate(
    val scheduledFor: LocalDate,
    val timeToAppointment: TimeToAppointment
) : Comparable<PotentialAppointmentDate> {

  companion object {
    fun from(
        appointmentTimes: List<TimeToAppointment>,
        userClock: UserClock
    ): List<PotentialAppointmentDate> {
      val today = LocalDate.now(userClock)
      return appointmentTimes
          .map { timeToAppointment -> today.plus(timeToAppointment) to timeToAppointment }
          .map { (appointmentDate, timeToAppointment) -> PotentialAppointmentDate(appointmentDate, timeToAppointment) }
    }
  }

  override fun compareTo(other: PotentialAppointmentDate): Int {
    return this.scheduledFor.compareTo(other.scheduledFor)
  }
}
