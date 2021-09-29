package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.widgets.UiEvent
import java.time.Duration
import java.util.UUID

sealed class TeleconsultMedicinesEvent : UiEvent

data class PatientMedicinesLoaded(val medicines: List<PrescribedDrug>) : TeleconsultMedicinesEvent()

object EditMedicinesClicked : TeleconsultMedicinesEvent() {
  override val analyticsName: String = "Teleconsult Medicines:Edit Clicked"
}

data class DrugDurationClicked(val prescription: PrescribedDrug) : TeleconsultMedicinesEvent() {
  override val analyticsName: String = "Teleconsult Medicines:Drug Duration Clicked"
}

data class DrugFrequencyClicked(val prescription: PrescribedDrug) : TeleconsultMedicinesEvent() {
  override val analyticsName: String = "Teleconsult Medicines:Drug Frequency Clicked"
}

data class DrugDurationChanged(
    val prescriptionUuid: UUID,
    val duration: Duration
) : TeleconsultMedicinesEvent() {
  override val analyticsName: String = "Teleconsult Medicines:Drug Duration Changed"
}

data class DrugFrequencyChanged(
    val prescriptionUuid: UUID,
    val frequency: MedicineFrequency
) : TeleconsultMedicinesEvent() {
  override val analyticsName: String = "Teleconsult Medicines:Drug Frequency Changed"
}
