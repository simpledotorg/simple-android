package org.simple.clinic.drugs.search

import androidx.paging.PagingData

sealed class DrugSearchEvent

data class DrugsSearchResultsLoaded(val searchResults: PagingData<Drug>) : DrugSearchEvent()
