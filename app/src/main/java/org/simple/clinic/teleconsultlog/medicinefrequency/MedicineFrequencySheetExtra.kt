package org.simple.clinic.teleconsultlog.medicinefrequency

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import java.util.UUID

@Parcelize
data class MedicineFrequencySheetExtra(
    val uuid: UUID,
    val name: String,
    val dosage: String?,
    val medicineFrequency: MedicineFrequency,
    val medicineFrequencyToFrequencyChoiceItemMap: Map<MedicineFrequency?, DrugFrequencyChoiceItem>
) : Parcelable
