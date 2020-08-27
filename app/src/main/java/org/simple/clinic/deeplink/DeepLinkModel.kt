package org.simple.clinic.deeplink

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class DeepLinkModel(
    val patientUuid: UUID?,
    val teleconsultRecordId: UUID?,
    val isLogTeleconsultDeepLink: Boolean
) : Parcelable {

  companion object {
    fun default(
        patientUuid: UUID?,
        teleconsultRecordId: UUID?,
        isLogTeleconsultDeepLink: Boolean
    ) = DeepLinkModel(
        patientUuid = patientUuid,
        teleconsultRecordId = teleconsultRecordId,
        isLogTeleconsultDeepLink = isLogTeleconsultDeepLink
    )
  }
}
