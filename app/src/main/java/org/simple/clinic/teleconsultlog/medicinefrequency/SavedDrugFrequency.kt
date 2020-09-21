package org.simple.clinic.teleconsultlog.medicinefrequency

import java.util.UUID

data class SavedDrugFrequency(
    val drugUuid: UUID,
    val frequency: MedicineFrequency
)
