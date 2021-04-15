package org.simple.clinic.editpatient.deletepatient

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class DeletePatientScreenKey(
    val patientUuid: UUID
) : FullScreenKey {
  @IgnoredOnParcel
  override val analyticsName: String = "Delete Patient"

  override fun layoutRes(): Int = R.layout.screen_delete_patient
}
