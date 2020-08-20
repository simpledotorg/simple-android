package org.simple.clinic.teleconsultlog.medicinefrequency

data class MedicineFrequencyModel(
    val medicineFrequency: MedicineFrequency
) {

  companion object {
    fun create(medicineFrequency: MedicineFrequency) = MedicineFrequencyModel(
        medicineFrequency = medicineFrequency
    )
  }

}
