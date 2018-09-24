package org.simple.clinic.medicalhistory

data class OngoingMedicalHistoryEntry(
    val hasHadHeartAttack: Boolean,
    val hasHadStroke: Boolean,
    val hasHadKidneyDisease: Boolean,
    val isOnTreatmentForHypertension: Boolean,
    val hasDiabetes: Boolean
)
