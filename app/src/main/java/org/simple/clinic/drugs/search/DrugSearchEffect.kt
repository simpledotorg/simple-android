package org.simple.clinic.drugs.search

import androidx.paging.PagingData

sealed class DrugSearchEffect

data class SearchDrugs(val searchQuery: String) : DrugSearchEffect()

data class SetDrugsSearchResults(val searchResults: PagingData<Drug>) : DrugSearchEffect()
