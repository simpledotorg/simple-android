package org.simple.clinic.summary.teleconsultation.status

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import java.util.UUID

@Parcelize
data class TeleconsultStatusModel(
    val teleconsultRecordId: UUID,
    val teleconsultStatus: TeleconsultStatus?
) : Parcelable {

  companion object {

    fun create(teleconsultRecordId: UUID) = TeleconsultStatusModel(
        teleconsultRecordId = teleconsultRecordId,
        teleconsultStatus = null
    )
  }

  val hasTeleconsultStatus: Boolean
    get() = teleconsultStatus != null

  fun teleconsultStatusChanged(teleconsultStatus: TeleconsultStatus): TeleconsultStatusModel {
    return copy(teleconsultStatus = teleconsultStatus)
  }
}
