package org.simple.clinic.drugs.selection.custom.drugfrequency

import org.simple.clinic.drugs.search.DrugFrequency

sealed class DrugFrequencyChoiceItem(val drugFrequency: DrugFrequency, val label: String)
