package org.simple.clinic.patient

import org.simple.clinic.patient.businessid.BusinessId

data class PatientProfile(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumbers: List<PatientPhoneNumber>,
    val businessIds: List<BusinessId>
)
