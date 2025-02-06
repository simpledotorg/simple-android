package org.simple.clinic.cvdrisk

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

data class LabBasedCVDRiskInput(
    val gender: Gender,
    val age: Int,
    val systolic: Int,
    val isSmoker: Answer = Answer.Unanswered,
    val diagnosedWithDiabetes: Answer = Answer.Unanswered,
    val cholesterol: Float? = null,
)
