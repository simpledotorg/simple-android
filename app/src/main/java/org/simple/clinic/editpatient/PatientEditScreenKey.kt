package org.simple.clinic.editpatient

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class PatientEditScreenKey(val patientUuid: UUID) : FullScreenKey {
  companion object {
    fun fromPatientUuid(uuid: UUID): PatientEditScreenKey =
        PatientEditScreenKey(uuid)
  }

  @IgnoredOnParcel
  override val analyticsName = "Edit Patient"

  override fun layoutRes() = R.layout.screen_patient_edit
}
