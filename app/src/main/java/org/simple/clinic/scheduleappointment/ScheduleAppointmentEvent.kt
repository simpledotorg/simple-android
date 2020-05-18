package org.simple.clinic.scheduleappointment

import org.simple.clinic.overdue.PotentialAppointmentDate

sealed class ScheduleAppointmentEvent

data class DefaultAppointmentDateLoaded(val potentialAppointmentDate: PotentialAppointmentDate): ScheduleAppointmentEvent()
