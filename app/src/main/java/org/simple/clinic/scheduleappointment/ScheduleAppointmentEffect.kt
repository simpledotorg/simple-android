package org.simple.clinic.scheduleappointment

import org.simple.clinic.facility.Facility
import org.threeten.bp.LocalDate
import java.util.UUID

sealed class ScheduleAppointmentEffect

object LoadDefaultAppointmentDate : ScheduleAppointmentEffect()

data class ShowDatePicker(val selectedDate: LocalDate) : ScheduleAppointmentEffect()

object LoadCurrentFacility : ScheduleAppointmentEffect()

data class ScheduleManualAppointment(
    val patientUuid: UUID,
    val scheduledForDate: LocalDate,
    val scheduledAtFacility: Facility
) : ScheduleAppointmentEffect()

data class ScheduleAutomaticAppointment(
    val patientUuid: UUID,
    val scheduledAtFacility: Facility
) : ScheduleAppointmentEffect()

object CloseSheet: ScheduleAppointmentEffect()

data class LoadPatientDefaulterStatus(val patientUuid: UUID): ScheduleAppointmentEffect()
