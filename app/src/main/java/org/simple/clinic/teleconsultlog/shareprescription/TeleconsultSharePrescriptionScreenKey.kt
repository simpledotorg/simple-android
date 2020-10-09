package org.simple.clinic.teleconsultlog.shareprescription

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

@Parcelize
class TeleconsultSharePrescriptionScreenKey(
    val patientUuid: UUID,
    val medicalInstructions : String?
): FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Teleconsultation Share Prescription Screen"

  override fun layoutRes(): Int = R.layout.screen_teleconsult_share_prescription
}
