package org.simple.clinic.search.results

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class PatientSearchResultsUpdate : Update<PatientSearchResultsModel, PatientSearchResultsEvent, PatientSearchResultsEffect> {

  override fun update(
      model: PatientSearchResultsModel,
      event: PatientSearchResultsEvent
  ): Next<PatientSearchResultsModel, PatientSearchResultsEffect> {
    return when (event) {
      is PatientSearchResultClicked -> {
        if (!model.hasAdditionalIdentifier)
          dispatch(OpenPatientSummary(event.patientUuid) as PatientSearchResultsEffect)
        else
          noChange()
      }
    }
  }
}
