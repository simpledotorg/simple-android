package org.simple.clinic.bloodsugar.history

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class BloodSugarHistoryScreenKey(
    val patientUuid: UUID
) : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName: String = "Blood Sugar History"

  override fun layoutRes(): Int {
    return R.layout.screen_blood_sugar_history
  }
}

