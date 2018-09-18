package org.simple.clinic.search.results

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class PatientSearchResultsScreenKey(
    val fullName: String,
    val age: String,
    val dateOfBirth: String
) : FullScreenKey {

  init {
    if (age.isNotEmpty() && dateOfBirth.isNotEmpty()) {
      throw AssertionError()
    }
  }

  @IgnoredOnParcel
  override val analyticsName = "Patient Search Results"

  override fun layoutRes() = R.layout.screen_patient_search_results
}
