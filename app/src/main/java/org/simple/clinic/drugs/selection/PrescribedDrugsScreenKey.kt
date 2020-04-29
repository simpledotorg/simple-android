package org.simple.clinic.drugs.selection

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class PrescribedDrugsScreenKey(val patientUuid: UUID) : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "Patient Drugs"

  override fun layoutRes(): Int {
    return R.layout.screen_patient_prescribed_drugs_entry
  }
}
