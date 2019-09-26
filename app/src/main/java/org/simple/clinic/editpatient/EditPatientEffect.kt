package org.simple.clinic.editpatient

import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber

sealed class EditPatientEffect

data class PrefillFormEffect(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) : EditPatientEffect()
