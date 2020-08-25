package org.simple.clinic.teleconsultlog.medicinefrequency

sealed class MedicineFrequencyEffect

data class LoadDefaultMedicineFrequency(val medicineFrequency: MedicineFrequency) : MedicineFrequencyEffect()

data class SaveMedicineFrequency(val medicineFrequency: MedicineFrequency) : MedicineFrequencyEffect()

