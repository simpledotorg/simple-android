package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

sealed class CustomDrugEntryEvent

data class DosageEdited(val dosage: String) : CustomDrugEntryEvent()

data class DosageFocusChanged(val hasFocus: Boolean) : CustomDrugEntryEvent()

data class EditFrequencyClicked(val frequency: DrugFrequency) : CustomDrugEntryEvent()

data class FrequencyEdited(val frequency: DrugFrequency) : CustomDrugEntryEvent()

object CustomDrugSaved : CustomDrugEntryEvent()
