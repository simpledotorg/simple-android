package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.time.Duration
import java.util.UUID

sealed class TeleconsultMedicinesEvent

data class PatientMedicinesLoaded(val medicines: List<PrescribedDrug>) : TeleconsultMedicinesEvent()

object EditMedicinesClicked : TeleconsultMedicinesEvent()

data class DrugDurationClicked(val prescription: PrescribedDrug) : TeleconsultMedicinesEvent()

data class DrugFrequencyClicked(val prescription: PrescribedDrug) : TeleconsultMedicinesEvent()

data class DrugDurationChanged(val prescriptionUuid: UUID, val duration: Duration) : TeleconsultMedicinesEvent()

data class DrugFrequencyChanged(val prescriptionUuid: UUID, val frequency: MedicineFrequency) : TeleconsultMedicinesEvent()
