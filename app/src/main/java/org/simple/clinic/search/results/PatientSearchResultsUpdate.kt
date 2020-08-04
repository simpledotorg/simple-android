package org.simple.clinic.search.results

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class PatientSearchResultsUpdate : Update<PatientSearchResultsModel, PatientSearchResultsEvent, PatientSearchResultsEffect> {

  override fun update(
      model: PatientSearchResultsModel,
      event: PatientSearchResultsEvent
  ): Next<PatientSearchResultsModel, PatientSearchResultsEffect> {
    return when (event) {
      is PatientSearchResultClicked -> {
        val effect = if (!model.hasAdditionalIdentifier)
          OpenPatientSummary(event.patientUuid)
        else
          OpenLinkIdWithPatientScreen(event.patientUuid, model.additionalIdentifier!!)

        dispatch(effect)
      }
    }
  }
}
