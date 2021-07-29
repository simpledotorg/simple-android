package org.simple.clinic.drugs.selection.custom

sealed class CustomDrugEntryEvent

data class DosageEdited(val dosage: String) : CustomDrugEntryEvent()

