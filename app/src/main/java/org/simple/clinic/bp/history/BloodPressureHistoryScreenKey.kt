package org.simple.clinic.bp.history

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey
import java.util.UUID

@Parcelize
data class BloodPressureHistoryScreenKey(
    val patientUuid: UUID
) : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName: String = "Blood Pressure History"

  override fun layoutRes(): Int {
    return R.layout.screen_bp_history
  }
}
