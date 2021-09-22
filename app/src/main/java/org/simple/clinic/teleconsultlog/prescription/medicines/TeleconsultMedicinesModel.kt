package org.simple.clinic.teleconsultlog.prescription.medicines

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.util.UUID

@Parcelize
data class TeleconsultMedicinesModel(
    val patientUuid: UUID,
    val medicines: List<PrescribedDrug>?,
    val medicineFrequencyToLabelMap: Map<MedicineFrequency?, DrugFrequencyLabel>?
) : Parcelable {

  companion object {

    fun create(patientUuid: UUID) = TeleconsultMedicinesModel(
        patientUuid = patientUuid,
        medicines = null,
        medicineFrequencyToLabelMap = null
    )
  }

  val hasMedicines: Boolean
    get() = medicines != null

  val medicinesNotNullorEmpty: Boolean
    get() = !medicines.isNullOrEmpty()

  val hasMedicineFrequencyToLabelMap
    get() = medicineFrequencyToLabelMap != null

  fun medicinesLoaded(medicines: List<PrescribedDrug>): TeleconsultMedicinesModel {
    return copy(medicines = medicines)
  }

  fun medicineFrequencyToLabelMapLoaded(medicineFrequencyToLabelMap: Map<MedicineFrequency?, DrugFrequencyLabel>?): TeleconsultMedicinesModel {
    return copy(medicineFrequencyToLabelMap = medicineFrequencyToLabelMap)
  }
}
