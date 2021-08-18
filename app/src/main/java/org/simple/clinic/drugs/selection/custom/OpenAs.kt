package org.simple.clinic.drugs.selection.custom

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

sealed class OpenAs : Parcelable {
  sealed class New : Parcelable, OpenAs() {
    @Parcelize
    data class FromDrugName(val patientUuid: UUID, val drugName: String) : New()

    @Parcelize
    data class FromDrugList(val patientUuid: UUID, val drugUuid: UUID) : New()
  }

  @Parcelize
  data class Update(val patientUuid: UUID, val prescribedDrugUuid: UUID) : OpenAs()
}
