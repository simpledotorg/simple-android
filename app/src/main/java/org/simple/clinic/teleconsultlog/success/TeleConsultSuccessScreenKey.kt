package org.simple.clinic.teleconsultlog.success

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey
import java.util.UUID

@Parcelize
data class TeleConsultSuccessScreenKey(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : Parcelable, FullScreenKey {
  @IgnoredOnParcel
  override val analyticsName: String = "TeleConsultation Success"

  override fun layoutRes(): Int {
    return R.layout.screen_teleconsult_success
  }
}
