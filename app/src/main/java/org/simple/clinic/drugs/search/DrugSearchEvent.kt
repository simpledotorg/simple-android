package org.simple.clinic.drugs.search

import androidx.paging.PagingData
import org.simple.clinic.widgets.UiEvent

sealed class DrugSearchEvent : UiEvent

data class DrugsSearchResultsLoaded(val searchResults: PagingData<Drug>) : DrugSearchEvent()

data class SearchQueryChanged(val searchQuery: String) : DrugSearchEvent()
