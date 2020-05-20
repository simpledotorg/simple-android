package org.simple.clinic.scheduleappointment

import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment
import org.threeten.bp.LocalDate
import java.util.UUID

sealed class ScheduleAppointmentEffect

object LoadDefaultAppointmentDate : ScheduleAppointmentEffect()

data class ShowDatePicker(val selectedDate: LocalDate) : ScheduleAppointmentEffect()

object LoadCurrentFacility : ScheduleAppointmentEffect()

data class ScheduleAppointment(
    val patientUuid: UUID,
    val scheduledForDate: LocalDate,
    val scheduledAtFacility: Facility,
    val type: Appointment.AppointmentType
) : ScheduleAppointmentEffect()

object CloseSheet: ScheduleAppointmentEffect()
