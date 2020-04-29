package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.widgets.UiEvent

data class CustomPrescriptionDrugNameTextChanged(val name: String) : UiEvent {
  override val analyticsName = "Drugs:Custom:Name Changed"
}

data class CustomPrescriptionDrugDosageTextChanged(val dosage: String) : UiEvent {
  override val analyticsName = "Drugs:Custom:Dosage Changed"
}

data class CustomPrescriptionDrugDosageFocusChanged(val hasFocus: Boolean) : UiEvent {
  override val analyticsName = "Drugs:Custom:Focused On Text Field"
}

object SaveCustomPrescriptionClicked : UiEvent {
  override val analyticsName = "Drugs:Custom:Saved"
}

object RemoveCustomPrescriptionClicked : UiEvent {
  override val analyticsName = "Drugs:Custom:Remove Clicked"
}
