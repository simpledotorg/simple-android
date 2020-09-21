package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug

sealed class TeleconsultMedicineItemEvent

data class DrugDurationButtonClicked(val prescribedDrug: PrescribedDrug) : TeleconsultMedicineItemEvent()

data class DrugFrequencyButtonClicked(val prescribedDrug: PrescribedDrug) : TeleconsultMedicineItemEvent()
