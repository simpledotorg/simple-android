package org.simple.clinic.home.overdue.phonemask

import org.simple.clinic.patient.Gender

data class PatientDetails(
    val phoneNumber: String,
    val name: String,
    val gender: Gender,
    val age: Int
)
