package org.simple.clinic.patient

data class PatientProfile(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumbers: List<PatientPhoneNumber>,
    val businessIds: List<BusinessId>
)
