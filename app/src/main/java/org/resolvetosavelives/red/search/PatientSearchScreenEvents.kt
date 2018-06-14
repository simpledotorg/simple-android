package org.resolvetosavelives.red.search

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.patient.PatientSearchResult
import org.resolvetosavelives.red.widgets.UiEvent

class BackButtonClicked : UiEvent

class CreateNewPatientClicked : UiEvent

class SearchQueryAgeFilterClicked : UiEvent

data class SearchQueryTextChanged(val query: String) : UiEvent

data class SearchResultClicked(val searchResult: PatientSearchResult) : UiEvent

@Parcelize
data class SearchQueryAgeChanged(val ageString: String) : UiEvent, Parcelable
