package org.simple.clinic.summary.linkId

import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.summary.PatientSummaryScreenKey

@Parcelize
data class LinkIdWithPatientSheetKey(
    val patientUuid: PatientUuid,
    val identifier: Identifier,
    val openedFrom: ScreenKey
) : ScreenKey() {

  override val analyticsName = "Link ID With Patient Sheet"

  override val type = ScreenType.Modal

  override fun instantiateFragment(): Fragment {
    return LinkIdWithPatientSheet()
  }
}
