package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ConfirmRemovePrescriptionDialogModel(
    val prescriptionUuid: UUID
) : Parcelable {

  companion object {
    fun create(prescriptionUuid: UUID) = ConfirmRemovePrescriptionDialogModel(prescriptionUuid = prescriptionUuid)
  }
}
