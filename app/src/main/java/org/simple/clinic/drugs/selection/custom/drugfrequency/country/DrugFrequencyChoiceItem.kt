package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import androidx.annotation.StringRes
import org.simple.clinic.drugs.search.DrugFrequency

data class DrugFrequencyChoiceItem(val drugFrequency: DrugFrequency?, @StringRes val labelResId: Int)
