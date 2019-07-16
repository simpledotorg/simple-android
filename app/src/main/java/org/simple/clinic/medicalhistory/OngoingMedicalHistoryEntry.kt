package org.simple.clinic.medicalhistory

import org.simple.clinic.medicalhistory.Answer.Unanswered

data class OngoingMedicalHistoryEntry(
    val hasHadHeartAttack: Answer = Unanswered,
    val hasHadStroke: Answer = Unanswered,
    val hasHadKidneyDisease: Answer = Unanswered,
    val diagnosedWithHypertension: Answer = Unanswered,
    val isOnTreatmentForHypertension: Answer = Unanswered,
    val hasDiabetes: Answer = Unanswered
)
