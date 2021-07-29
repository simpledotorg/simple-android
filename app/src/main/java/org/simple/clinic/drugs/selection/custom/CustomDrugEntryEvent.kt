package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.search.DrugFrequency

sealed class CustomDrugEntryEvent

data class DosageEdited(val dosage: String) : CustomDrugEntryEvent()

data class EditFrequencyClicked(val frequency: DrugFrequency) : CustomDrugEntryEvent()

data class FrequencyEdited(val frequency: DrugFrequency) : CustomDrugEntryEvent()

object AddMedicineButtonClicked : CustomDrugEntryEvent()

object CustomDrugSaved : CustomDrugEntryEvent()

data class CustomDrugFetched(val prescription: PrescribedDrug) : CustomDrugEntryEvent()

object ExistingDrugRemoved : CustomDrugEntryEvent()

object RemoveDrugButtonClicked : CustomDrugEntryEvent()
