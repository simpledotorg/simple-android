package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import org.threeten.bp.Instant
import java.util.UUID

@Parcelize
data class PatientSummaryScreenKey(
    val patientUuid: UUID,
    val intention: OpenIntention,
    val screenCreatedTimestamp: Instant
) : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "Patient Summary"

  override fun layoutRes(): Int {
    return R.layout.screen_patient_summary
  }
}
