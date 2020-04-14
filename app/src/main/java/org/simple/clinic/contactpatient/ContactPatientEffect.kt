package org.simple.clinic.contactpatient

import java.util.UUID

sealed class ContactPatientEffect

data class LoadPatientProfile(val patientUuid: UUID): ContactPatientEffect()

data class LoadLatestOverdueAppointment(val patientUuid: UUID): ContactPatientEffect()

data class DirectCallWithAutomaticDialer(val patientPhoneNumber: String): ContactPatientEffect()

data class DirectCallWithManualDialer(val patientPhoneNumber: String): ContactPatientEffect()

data class MaskedCallWithAutomaticDialer(val patientPhoneNumber: String, val proxyPhoneNumber: String): ContactPatientEffect()

data class MaskedCallWithManualDialer(val patientPhoneNumber: String, val proxyPhoneNumber: String): ContactPatientEffect()
