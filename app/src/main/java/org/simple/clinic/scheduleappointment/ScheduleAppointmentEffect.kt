package org.simple.clinic.scheduleappointment

import org.threeten.bp.LocalDate

sealed class ScheduleAppointmentEffect

object LoadDefaultAppointmentDate: ScheduleAppointmentEffect()

data class ShowDatePicker(val selectedDate: LocalDate): ScheduleAppointmentEffect()
