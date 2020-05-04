package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.widgets.UiEvent

sealed class CustomPrescriptionEntryEvent : UiEvent

data class CustomPrescriptionDrugNameTextChanged(val name: String) : CustomPrescriptionEntryEvent() {
  override val analyticsName = "Drugs:Custom:Name Changed"
}
