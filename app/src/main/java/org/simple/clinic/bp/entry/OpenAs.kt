package org.simple.clinic.bp.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

sealed interface OpenAs : Parcelable {

  val patientUuid: UUID

  @Parcelize
  data class New(override val patientUuid: UUID) : OpenAs

  @Parcelize
  data class Update(val bpUuid: UUID, override val patientUuid: UUID) : OpenAs
}
