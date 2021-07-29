package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.Drug
import org.simple.clinic.drugs.search.DrugFrequency
import java.util.UUID

data class CustomDrugEntryModel(
    val drugName: String,
    val dosage: String?,
    val frequency: DrugFrequency?,
    val rxNormCode: String?
) {
  companion object {
    fun default(
        drug: Drug?,
        drugName: String,
    ) = CustomDrugEntryModel(
        drugName = drug?.name ?: drugName,
        dosage = drug?.dosage,
        frequency = drug?.frequency ?: DrugFrequency.Unknown("None"),
        rxNormCode = drug?.rxNormCode)
  }

  val hasDrugFrequency
    get() = frequency != null

  val hasDrugDosage
    get() = !dosage.isNullOrEmpty()

  fun dosageEdited(dosage: String?): CustomDrugEntryModel {
    return copy(dosage = dosage)
  }

  fun frequencyEdited(frequency: DrugFrequency): CustomDrugEntryModel {
    return copy(frequency = frequency)
  }
}
