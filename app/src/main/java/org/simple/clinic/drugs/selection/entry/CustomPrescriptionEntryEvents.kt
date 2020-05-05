package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.widgets.UiEvent

data class CustomPrescriptionDrugDosageFocusChanged(val hasFocus: Boolean) : UiEvent {
  override val analyticsName = "Drugs:Custom:Focused On Text Field"
}

object RemoveCustomPrescriptionClicked : UiEvent {
  override val analyticsName = "Drugs:Custom:Remove Clicked"
}
