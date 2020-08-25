package org.simple.clinic.teleconsultlog.medicinefrequency

import org.simple.clinic.widgets.UiEvent

sealed class MedicineFrequencyEvent : UiEvent

data class MedicineFrequencyChanged(val medicineFrequency: MedicineFrequency) : MedicineFrequencyEvent() {
  override val analyticsName: String = "Medicine Frequency:Medicine Frequency Changed"
}

data class SaveMedicineFrequencyClicked(val medicineFrequency: MedicineFrequency) : MedicineFrequencyEvent() {
  override val analyticsName: String = "Save Medicine Frequency:Save Medicine Frequency Clicked"
}
