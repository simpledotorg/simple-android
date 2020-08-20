package org.simple.clinic.teleconsultlog.medicinefrequency

import org.simple.clinic.widgets.UiEvent

sealed class MedicineFrequencyEvent : UiEvent

data class SaveMedicineFrequencyClicked(val medicineFrequency: MedicineFrequency) : MedicineFrequencyEvent() {
  override val analyticsName: String = "Save Medicine Frequency:Save Medicine Frequency Clicked"
}
