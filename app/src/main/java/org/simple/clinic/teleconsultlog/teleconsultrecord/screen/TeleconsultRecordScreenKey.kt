package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class TeleconsultRecordScreenKey(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName: String = "Teleconsult Record Screen"

  override fun layoutRes(): Int {
    return R.layout.screen_teleconsult_record
  }
}
