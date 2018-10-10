package org.simple.clinic.patient

data class PatientSaveModel(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumbers: List<PatientPhoneNumber>
)
