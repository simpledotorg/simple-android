package org.simple.clinic.contactpatient

import org.simple.clinic.overdue.Appointment
import java.time.LocalDate
import java.util.UUID

sealed class ContactPatientEffect

data class LoadContactPatientProfile(val patientUuid: UUID) : ContactPatientEffect()

data class LoadLatestOverdueAppointment(val patientUuid: UUID) : ContactPatientEffect()

object LoadCurrentFacility : ContactPatientEffect()

data class MaskedCallWithManualDialer(
    val patientPhoneNumber: String,
    val proxyPhoneNumber: String
) : ContactPatientEffect()

object CloseScreen : ContactPatientEffect()

data class MarkPatientAsAgreedToVisit(val appointment: Appointment) : ContactPatientEffect()

data class ShowManualDatePicker(
    val preselectedDate: LocalDate,
    val datePickerBounds: ClosedRange<LocalDate>
) : ContactPatientEffect()

data class SetReminderForAppointment(
    val appointment: Appointment,
    val reminderDate: LocalDate
) : ContactPatientEffect()

data class OpenRemoveOverdueAppointmentScreen(
    val appointment: Appointment
) : ContactPatientEffect()

data class LoadCallResultForAppointment(val appointmentId: UUID) : ContactPatientEffect()

sealed class ContactPatientViewEffect : ContactPatientEffect()

data class DirectCallWithAutomaticDialer(val patientPhoneNumber: String) : ContactPatientViewEffect()

data class DirectCallWithManualDialer(val patientPhoneNumber: String) : ContactPatientViewEffect()

data class MaskedCallWithAutomaticDialer(
    val patientPhoneNumber: String,
    val proxyPhoneNumber: String
) : ContactPatientViewEffect()
