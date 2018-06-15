package org.simple.clinic.search

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent

class BackButtonClicked : UiEvent

class CreateNewPatientClicked : UiEvent

class SearchQueryAgeFilterClicked : UiEvent

data class SearchQueryTextChanged(val query: String) : UiEvent

data class SearchResultClicked(val searchResult: PatientSearchResult) : UiEvent

@Parcelize
data class SearchQueryAgeChanged(val ageString: String) : UiEvent, Parcelable
