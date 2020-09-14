package org.simple.clinic.teleconsultlog.prescription

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
data class TeleconsultPrescriptionScreenKey(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Teleconsultation Prescription"

  override fun layoutRes(): Int = R.layout.screen_teleconsult_prescription
}
