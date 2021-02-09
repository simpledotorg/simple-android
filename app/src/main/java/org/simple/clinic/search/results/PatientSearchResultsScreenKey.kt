package org.simple.clinic.search.results

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.patient.PatientSearchCriteria

@Parcelize
data class PatientSearchResultsScreenKey(val criteria: PatientSearchCriteria) : ScreenKey() {

  @IgnoredOnParcel
  override val analyticsName = "Patient Search Results"

  override fun instantiateFragment() = PatientSearchResultsScreen()
}
