package org.simple.clinic.teleconsultlog.shareprescription

import androidx.fragment.app.Fragment
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import java.util.UUID

@Parcelize
class TeleconsultSharePrescriptionScreenKey(
    val patientUuid: UUID,
    val medicalInstructions: String?
) : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName: String = "Teleconsultation Share Prescription Screen"

  override fun instantiateFragment(): Fragment {
    return TeleconsultSharePrescriptionScreen()
  }
}
