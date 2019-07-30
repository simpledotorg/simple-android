package org.simple.clinic.search.results

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class PatientSearchResultsScreenKey(val criteria: PatientSearchCriteria) : FullScreenKey {

  constructor(fullName: String) : this(PatientSearchCriteria.Name(fullName))

  val fullName: String
    get() = (criteria as PatientSearchCriteria.Name).patientName

  @IgnoredOnParcel
  override val analyticsName = "Patient Search Results"

  override fun layoutRes() = R.layout.screen_patient_search_results
}
