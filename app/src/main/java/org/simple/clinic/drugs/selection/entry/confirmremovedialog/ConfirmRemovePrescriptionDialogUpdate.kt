package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class ConfirmRemovePrescriptionDialogUpdate : Update<ConfirmRemovePrescriptionDialogModel,
    ConfirmRemovePrescriptionDialogEvent, ConfirmRemovePrescriptionDialogEffect> {
  override fun update(
      model: ConfirmRemovePrescriptionDialogModel,
      event: ConfirmRemovePrescriptionDialogEvent
  ): Next<ConfirmRemovePrescriptionDialogModel,
      ConfirmRemovePrescriptionDialogEffect> {
    return when (event) {
      RemovePrescriptionClicked -> dispatch(RemovePrescription(model.prescriptionUuid))
      PrescriptionRemoved -> dispatch(CloseDialog)
    }
  }
}
