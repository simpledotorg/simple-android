package org.simple.clinic.drugs.selection.custom

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

sealed class OpenAs : Parcelable {
  sealed class New : Parcelable, OpenAs() {
    @Parcelize
    data class FromDrugName(val drugName: String) : New()

    @Parcelize
    data class FromDrugList(val drugUuid: UUID) : New()
  }

  @Parcelize
  data class Update(val prescribedDrugUuid: UUID) : OpenAs()
}
