package org.simple.clinic.drugs.search

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class DrugSearchUpdate : Update<DrugSearchModel, DrugSearchEvent, DrugSearchEffect> {

  override fun update(
      model: DrugSearchModel,
      event: DrugSearchEvent
  ): Next<DrugSearchModel, DrugSearchEffect> {
    return when (event) {
      is DrugsSearchResultsLoaded -> dispatch(SetDrugsSearchResults(event.searchResults))
    }
  }
}
