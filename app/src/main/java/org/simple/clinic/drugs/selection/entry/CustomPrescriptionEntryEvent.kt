package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.widgets.UiEvent

sealed class CustomPrescriptionEntryEvent : UiEvent

data class CustomPrescriptionDrugNameTextChanged(val name: String) : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Name Changed"
}

data class CustomPrescriptionDrugDosageTextChanged(val dosage: String) : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Dosage Changed"
}

object SaveCustomPrescriptionClicked : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Saved"
}

object CustomPrescriptionSaved : CustomPrescriptionEntryEvent()
