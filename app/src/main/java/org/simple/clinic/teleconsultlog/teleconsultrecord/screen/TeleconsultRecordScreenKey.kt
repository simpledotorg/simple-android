package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import java.util.UUID

@Parcelize
data class TeleconsultRecordScreenKey(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : ScreenKey(), Parcelable {

  @IgnoredOnParcel
  override val analyticsName: String = "Teleconsult Record Screen"

  override fun instantiateFragment() = TeleconsultRecordScreen()
}
