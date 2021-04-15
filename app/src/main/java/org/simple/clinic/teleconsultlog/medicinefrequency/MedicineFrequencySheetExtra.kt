package org.simple.clinic.teleconsultlog.medicinefrequency

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class MedicineFrequencySheetExtra(
    val uuid: UUID,
    val name: String,
    val dosage: String?,
    val medicineFrequency: MedicineFrequency
) : Parcelable
