package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import org.simple.clinic.widgets.UiEvent

sealed class ConfirmRemovePrescriptionDialogEvent : UiEvent

object PrescriptionRemoved : ConfirmRemovePrescriptionDialogEvent()

object RemovePrescriptionClicked : ConfirmRemovePrescriptionDialogEvent() {
  override val analyticsName = "Confirm Remove Prescription:Remove Clicked"
}
