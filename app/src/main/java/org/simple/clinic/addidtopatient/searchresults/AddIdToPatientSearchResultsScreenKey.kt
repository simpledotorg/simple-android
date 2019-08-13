package org.simple.clinic.addidtopatient.searchresults

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class AddIdToPatientSearchResultsScreenKey(
    val searchCriteria: PatientSearchCriteria,
    val identifier: Identifier
) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Add ID to Patient: Search Results"

  override fun layoutRes() = R.layout.screen_addidtopatientsearchresults
}
