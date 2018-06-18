package org.simple.clinic.drugs.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class PrescribedDrugsEntryScreenKey(val patientUuid: UUID) : FullScreenKey, Parcelable {

  override fun layoutRes(): Int {
    return R.layout.screen_patient_prescribed_drugs_entry
  }
}
