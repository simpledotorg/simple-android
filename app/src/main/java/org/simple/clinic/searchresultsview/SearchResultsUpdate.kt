package org.simple.clinic.searchresultsview

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class SearchResultsUpdate : Update<SearchResultsModel, SearchResultsEvent, SearchResultsEffect> {

  override fun update(
      model: SearchResultsModel,
      event: SearchResultsEvent
  ): Next<SearchResultsModel, SearchResultsEffect> {
    return when (event) {
      is SearchPatientWithCriteria -> dispatch(SearchWithCriteria(event.criteria))
      is SearchResultsLoaded -> next(model.withSearchResults(event.patientSearchResults))
    }
  }
}
