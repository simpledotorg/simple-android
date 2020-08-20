package org.simple.clinic.teleconsultlog.medicinefrequency

sealed class MedicineFrequencyEffect

data class LoadDefaultMedicineFrequency(val medicineFrequency: MedicineFrequency) : MedicineFrequencyEffect()
