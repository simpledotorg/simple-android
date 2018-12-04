package org.simple.clinic.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.UNKNOWN

data class OngoingMedicalHistoryEntry(
    val hasHadHeartAttack: Answer = UNKNOWN,
    val hasHadStroke: Answer = UNKNOWN,
    val hasHadKidneyDisease: Answer = UNKNOWN,
    val diagnosedWithHypertension: Answer = UNKNOWN,
    val isOnTreatmentForHypertension: Answer = UNKNOWN,
    val hasDiabetes: Answer = UNKNOWN
)
