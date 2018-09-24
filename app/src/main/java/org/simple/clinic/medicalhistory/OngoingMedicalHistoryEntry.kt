package org.simple.clinic.medicalhistory

data class OngoingMedicalHistoryEntry(
    val hasHadHeartAttack: Boolean = false,
    val hasHadStroke: Boolean = false,
    val hasHadKidneyDisease: Boolean = false,
    val isOnTreatmentForHypertension: Boolean = false,
    val hasDiabetes: Boolean = false
)
