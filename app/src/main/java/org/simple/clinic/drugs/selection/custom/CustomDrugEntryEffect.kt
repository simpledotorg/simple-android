package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

sealed class CustomDrugEntryEffect

data class ShowEditFrequencyDialog(val frequency: DrugFrequency) : CustomDrugEntryEffect()

data class SetDrugFrequency(val frequency: DrugFrequency?) : CustomDrugEntryEffect()

data class SetDrugDosage(val dosage: String?) : CustomDrugEntryEffect()

data class SetSheetTitle(
    val name: String?,
    val dosage: String?,
    val frequency: DrugFrequency?
) : CustomDrugEntryEffect()
