package org.simple.clinic.teleconsultlog.prescription

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.compat.FullScreenKey
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
