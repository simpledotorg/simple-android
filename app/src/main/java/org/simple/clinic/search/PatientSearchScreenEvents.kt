package org.simple.clinic.search

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent

class CreateNewPatientClicked : UiEvent {
  override val analyticsName = "Patient Search:Create New Patient Clicked"
}

class SearchQueryAgeFilterClicked : UiEvent {
  override val analyticsName = "Patient Search:Filter By Age Clicked"
}

data class SearchQueryNameChanged(val name: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Changed"
}

class SearchClicked : UiEvent {
  override val analyticsName = "Patient Search:Search Clicked"
}

data class SearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Patient Search:Search Result Clicked"
}

@Parcelize
data class SearchQueryAgeChanged(val ageString: String) : UiEvent, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "Patient Search:Search Query Age Changed"
}
