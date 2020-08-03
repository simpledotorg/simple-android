package org.simple.clinic.search

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class PatientSearchUpdate : Update<PatientSearchModel, PatientSearchEvent, PatientSearchEffect> {

  override fun update(model: PatientSearchModel, event: PatientSearchEvent): Next<PatientSearchModel, PatientSearchEffect> {
    return when(event) {
      is SearchQueryTextChanged -> next(model.queryChanged(event.text))
    }
  }
}
