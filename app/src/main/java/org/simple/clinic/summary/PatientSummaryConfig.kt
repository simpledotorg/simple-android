package org.simple.clinic.summary

data class PatientSummaryConfig(
    val numberOfBpPlaceholders: Int,
    val numberOfBpsToDisplay: Int,
    val isUpdatePhoneDialogEnabled: Boolean,
    val isNewPrescriptionScreenEnabled: Boolean
)
