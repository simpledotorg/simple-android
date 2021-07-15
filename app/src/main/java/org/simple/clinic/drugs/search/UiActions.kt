package org.simple.clinic.drugs.search

import androidx.paging.PagingData

interface UiActions {
  fun setDrugSearchResults(searchResults: PagingData<Drug>)
}
