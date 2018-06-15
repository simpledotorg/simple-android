package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class PatientSummaryScreenKey(val patientUuid: UUID, val caller: PatientSummaryCaller) : FullScreenKey, Parcelable {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_summary
  }
}
