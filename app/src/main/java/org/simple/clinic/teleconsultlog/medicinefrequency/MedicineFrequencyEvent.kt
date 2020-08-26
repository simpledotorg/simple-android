package org.simple.clinic.teleconsultlog.medicinefrequency

import org.simple.clinic.widgets.UiEvent

sealed class MedicineFrequencyEvent : UiEvent

data class MedicineFrequencyChanged(val medicineFrequency: MedicineFrequency) : MedicineFrequencyEvent() {
  override val analyticsName: String = "Medicine Frequency:Medicine Frequency Changed"
}

object SaveMedicineFrequencyClicked : MedicineFrequencyEvent() {
  override val analyticsName: String = "Save Medicine Frequency:Save Clicked"
}
