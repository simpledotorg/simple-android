package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.widgets.UiEvent

sealed class CustomPrescriptionEntryEvent : UiEvent

data class CustomPrescriptionDrugNameTextChanged(val name: String) : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Name Changed"
}

data class CustomPrescriptionDrugDosageTextChanged(val dosage: String) : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Dosage Changed"
}

data class CustomPrescriptionDrugDosageFocusChanged(val hasFocus: Boolean) : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Focused On Text Field"
}

object SaveCustomPrescriptionClicked : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Saved"
}

object RemoveCustomPrescriptionClicked : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Remove Clicked"
}

object CustomPrescriptionSaved : CustomPrescriptionEntryEvent()

data class CustomPrescriptionFetched(val prescription: PrescribedDrug) : CustomPrescriptionEntryEvent()
