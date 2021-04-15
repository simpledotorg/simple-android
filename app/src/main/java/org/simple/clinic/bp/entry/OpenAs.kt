package org.simple.clinic.bp.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

sealed class OpenAs : Parcelable {

  @Parcelize
  data class New(val patientUuid: UUID): OpenAs()

  @Parcelize
  data class Update(val bpUuid: UUID): OpenAs()
}
