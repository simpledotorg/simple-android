package org.simple.clinic.instantsearch

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class InstantSearchUpdate : Update<InstantSearchModel, InstantSearchEvent, InstantSearchEffect> {

  override fun update(model: InstantSearchModel, event: InstantSearchEvent): Next<InstantSearchModel, InstantSearchEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> next(model.facilityLoaded(event.facility), LoadAllPatients(event.facility))
      is AllPatientsLoaded -> noChange()
      is SearchResultsLoaded -> noChange()
    }
  }
}
