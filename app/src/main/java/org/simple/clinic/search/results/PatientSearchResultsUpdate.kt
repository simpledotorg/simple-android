package org.simple.clinic.search.results

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class PatientSearchResultsUpdate : Update<PatientSearchResultsModel, PatientSearchResultsEvent, PatientSearchResultsEffect> {

  override fun update(
      model: PatientSearchResultsModel,
      event: PatientSearchResultsEvent
  ): Next<PatientSearchResultsModel, PatientSearchResultsEffect> {
    return noChange()
  }
}
