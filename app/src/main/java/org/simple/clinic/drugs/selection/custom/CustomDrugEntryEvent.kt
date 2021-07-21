package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

sealed class CustomDrugEntryEvent

data class DosageEdited(val dosage: String) : CustomDrugEntryEvent()

data class EditFrequencyClicked(val frequency: DrugFrequency) : CustomDrugEntryEvent()
