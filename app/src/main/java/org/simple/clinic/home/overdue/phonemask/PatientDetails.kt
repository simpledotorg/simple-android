package org.simple.clinic.home.overdue.phonemask

data class PatientDetails(
    val phoneNumber: String?,
    val name: String,
    val genderLetterRes: Int,
    val age: Int
)
