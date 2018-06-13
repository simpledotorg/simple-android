package org.resolvetosavelives.red.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class PatientSummaryScreenKey(val patientUuid: UUID, val caller: PatientSummaryCaller) : FullScreenKey, Parcelable {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_summary
  }
}
