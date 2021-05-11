package org.simple.clinic.scanid

data class PatientPrefillInfo(
    val fullName: String,
    val gender: String,
    val state: String?,
    val district: String?,
    val dateOfBirth: String,
    val address: String
)
