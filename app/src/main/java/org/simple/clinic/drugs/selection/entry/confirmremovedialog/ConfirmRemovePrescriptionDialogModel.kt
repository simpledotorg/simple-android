package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ConfirmRemovePrescriptionDialogModel : Parcelable {

  companion object {
    fun create() = ConfirmRemovePrescriptionDialogModel()
  }
}
