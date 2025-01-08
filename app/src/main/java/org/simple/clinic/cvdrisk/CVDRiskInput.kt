package org.simple.clinic.cvdrisk

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

data class CVDRiskInput(
    val gender: Gender,
    val age: Int,
    val systolic: Int,
    val isSmoker: Answer = Answer.Unanswered,
    val bmi: Float? = null
)
