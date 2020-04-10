package org.simple.clinic.patientcontact

import java.util.UUID

sealed class PatientContactEffect

data class LoadPatientProfile(val patientUuid: UUID): PatientContactEffect()

data class LoadLatestOverdueAppointment(val patientUuid: UUID): PatientContactEffect()

data class DirectCallWithAutomaticDialer(val patientPhoneNumber: String): PatientContactEffect()

data class DirectCallWithManualDialer(val patientPhoneNumber: String): PatientContactEffect()

data class MaskedCallWithAutomaticDialer(val patientPhoneNumber: String, val proxyPhoneNumber: String): PatientContactEffect()

data class MaskedCallWithManualDialer(val patientPhoneNumber: String, val proxyPhoneNumber: String): PatientContactEffect()
