package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

sealed class CustomDrugEntryEffect

data class ShowEditFrequencyDialog(val frequency: DrugFrequency) : CustomDrugEntryEffect()
