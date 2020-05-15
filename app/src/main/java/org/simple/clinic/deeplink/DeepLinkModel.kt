package org.simple.clinic.deeplink

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class DeepLinkModel(
    val patientUuid: UUID?
) : Parcelable {

  companion object {
    fun default(patientUuid: UUID?) = DeepLinkModel(patientUuid = patientUuid)
  }
}
