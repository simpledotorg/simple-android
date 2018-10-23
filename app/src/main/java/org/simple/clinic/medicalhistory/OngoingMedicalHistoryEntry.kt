package org.simple.clinic.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.UNSELECTED

data class OngoingMedicalHistoryEntry(
    val hasHadHeartAttack: Answer = UNSELECTED,
    val hasHadStroke: Answer = UNSELECTED,
    val hasHadKidneyDisease: Answer = UNSELECTED,
    val diagnosedWithHypertension: Answer = UNSELECTED,
    val isOnTreatmentForHypertension: Answer = UNSELECTED,
    val hasDiabetes: Answer = UNSELECTED
)
