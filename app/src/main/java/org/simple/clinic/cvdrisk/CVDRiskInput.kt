package org.simple.clinic.cvdrisk

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

data class CVDRiskInput(
    val cvdRiskData: CVDRiskCalculationSheet?,
    val gender: Gender,
    val age: Int,
    val sbp: Int,
    val isSmoker: Answer = Answer.Unanswered,
    val bmi: Double? = null
)