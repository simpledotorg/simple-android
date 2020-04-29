package org.simple.clinic.bp.entry.confirmremovebloodpressure

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ConfirmRemovePrescriptionDialogCreated(val prescribedDrugUuid: UUID) : UiEvent {
  override val analyticsName = "Confirm Remove Prescription:Dialog Created"
}

object RemovePrescriptionClicked : UiEvent {
  override val analyticsName = "Confirm Remove Prescription:Remove Clicked"
}
