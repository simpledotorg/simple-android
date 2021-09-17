package org.simple.clinic.teleconsultlog.prescription.medicines

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.util.UUID

@Parcelize
data class TeleconsultMedicinesModel(
    val patientUuid: UUID,
    val medicines: List<PrescribedDrug>?,
    val medicineFrequencyToFrequencyChoiceItemMap: Map<MedicineFrequency?, DrugFrequencyChoiceItem>?
) : Parcelable {

  companion object {

    fun create(patientUuid: UUID) = TeleconsultMedicinesModel(
        patientUuid = patientUuid,
        medicines = null,
        medicineFrequencyToFrequencyChoiceItemMap = null
    )
  }

  val hasMedicines: Boolean
    get() = medicines != null

  val medicinesNotNullorEmpty: Boolean
    get() = !medicines.isNullOrEmpty()

  val hasMedicineFrequencyToFrequencyChoiceItemMap
    get() = medicineFrequencyToFrequencyChoiceItemMap != null

  fun medicinesLoaded(medicines: List<PrescribedDrug>): TeleconsultMedicinesModel {
    return copy(medicines = medicines)
  }

  fun medicineFrequencyToFrequencyChoiceItemMapLoaded(
      medicineFrequencyToFrequencyChoiceItemMap: Map<MedicineFrequency?, DrugFrequencyChoiceItem>?
  ) = copy(medicineFrequencyToFrequencyChoiceItemMap = medicineFrequencyToFrequencyChoiceItemMap)
}
