package org.simple.clinic.scheduleappointment

import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment
import java.time.LocalDate
import java.util.UUID

sealed class ScheduleAppointmentEffect

object LoadDefaultAppointmentDate : ScheduleAppointmentEffect()

data class ShowDatePicker(val selectedDate: LocalDate) : ScheduleAppointmentEffect()

data class LoadAppointmentFacilities(val patientUuid: UUID) : ScheduleAppointmentEffect()

data class ScheduleAppointmentForPatient(
    val patientUuid: UUID,
    val scheduledForDate: LocalDate,
    val scheduledAtFacility: Facility,
    val type: Appointment.AppointmentType
) : ScheduleAppointmentEffect()

object CloseSheet : ScheduleAppointmentEffect()

data class LoadPatientDefaulterStatus(val patientUuid: UUID) : ScheduleAppointmentEffect()

data class LoadTeleconsultRecord(val patientUuid: UUID) : ScheduleAppointmentEffect()
