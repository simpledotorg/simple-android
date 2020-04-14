package org.simple.clinic.overdue

import org.threeten.bp.LocalDate

data class PotentialAppointmentDate(
    val scheduledFor: LocalDate,
    val timeToAppointment: TimeToAppointment
) : Comparable<PotentialAppointmentDate> {
  override fun compareTo(other: PotentialAppointmentDate): Int {
    return this.scheduledFor.compareTo(other.scheduledFor)
  }
}
