package org.simple.clinic.drugs.selection.dosage

sealed class DosagePickerEffect

data class LoadProtocolDrugsByName(val drugName: String) : DosagePickerEffect()
