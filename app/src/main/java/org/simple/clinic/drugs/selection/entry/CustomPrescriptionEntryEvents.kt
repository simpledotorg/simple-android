package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.widgets.UiEvent

data class CustomPrescriptionSheetCreated(val openAs: OpenAs) : UiEvent {
  override val analyticsName = "Drugs:Custom:Show Prescription Sheet"
}

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
