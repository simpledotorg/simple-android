package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class ConfirmRemovePrescriptionDialogUpdate : Update<ConfirmRemovePrescriptionDialogModel,
    ConfirmRemovePrescriptionDialogEvent, ConfirmRemovePrescriptionDialogEffect> {
  override fun update(
      model: ConfirmRemovePrescriptionDialogModel,
      event: ConfirmRemovePrescriptionDialogEvent
  ): Next<ConfirmRemovePrescriptionDialogModel,
      ConfirmRemovePrescriptionDialogEffect> = noChange()
}
