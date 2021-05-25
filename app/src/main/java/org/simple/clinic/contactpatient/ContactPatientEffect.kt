package org.simple.clinic.contactpatient

import java.time.LocalDate
import java.util.UUID

sealed class ContactPatientEffect

data class LoadPatientProfile(val patientUuid: UUID) : ContactPatientEffect()

data class LoadLatestOverdueAppointment(val patientUuid: UUID) : ContactPatientEffect()

data class DirectCallWithAutomaticDialer(val patientPhoneNumber: String) : ContactPatientEffect()

data class DirectCallWithManualDialer(val patientPhoneNumber: String) : ContactPatientEffect()

data class MaskedCallWithAutomaticDialer(
    val patientPhoneNumber: String,
    val proxyPhoneNumber: String
) : ContactPatientEffect()

data class MaskedCallWithManualDialer(
    val patientPhoneNumber: String,
    val proxyPhoneNumber: String
) : ContactPatientEffect()

object CloseScreen : ContactPatientEffect()

data class MarkPatientAsAgreedToVisit(val appointmentUuid: UUID) : ContactPatientEffect()

data class ShowManualDatePicker(
    val preselectedDate: LocalDate,
    val datePickerBounds: ClosedRange<LocalDate>
) : ContactPatientEffect()

data class SetReminderForAppointment(
    val appointmentUuid: UUID,
    val reminderDate: LocalDate
) : ContactPatientEffect()

data class OpenRemoveOverdueAppointmentScreen(
    val appointmentId: UUID,
    val patientId: UUID
) : ContactPatientEffect()
