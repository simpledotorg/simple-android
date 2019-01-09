package org.simple.clinic.drugs.selectionv2

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class PrescribedDrugsScreenKeyV2(val patientUuid: UUID) : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "Patient Drugs"

  override fun layoutRes(): Int {
    return R.layout.screen_patient_prescribed_drugs_entry_v2
  }
}
