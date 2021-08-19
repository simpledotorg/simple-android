package org.simple.clinic.drugs.search

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class DrugSearchUpdate : Update<DrugSearchModel, DrugSearchEvent, DrugSearchEffect> {

  override fun update(
      model: DrugSearchModel,
      event: DrugSearchEvent
  ): Next<DrugSearchModel, DrugSearchEffect> {
    return when (event) {
      is DrugsSearchResultsLoaded -> dispatch(SetDrugsSearchResults(event.searchResults))
      is SearchQueryChanged -> searchDrugs(model, event)
      is DrugListItemClicked -> dispatch(OpenCustomDrugEntrySheetFromDrugList(event.drugId, event.patientUuid))
      is NewCustomDrugClicked -> dispatch(OpenCustomDrugEntrySheetFromDrugName(event.drugName, event.patientUuid))
    }
  }

  private fun searchDrugs(
      model: DrugSearchModel,
      event: SearchQueryChanged
  ): Next<DrugSearchModel, DrugSearchEffect> {
    val searchQuery = event.searchQuery
    val searchQueryChangedModel = model.searchQueryChanged(searchQuery)

    return if (searchQuery.isNotBlank()) {
      next(searchQueryChangedModel, SearchDrugs(searchQuery))
    } else {
      next(searchQueryChangedModel)
    }
  }
}
