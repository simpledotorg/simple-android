package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.search.DrugFrequency

@Parcelize
data class DrugFrequencyChoiceItem(
    val drugFrequency: DrugFrequency?,
    val label: String
) : Parcelable
