package org.simple.clinic.identifiersearchresult

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class IdentifierSearchResultUpdate : Update<IdentifierSearchResultState, IdentifierSearchResultEvent, IdentifierSearchResultEffect> {

  override fun update(
      model: IdentifierSearchResultState,
      event: IdentifierSearchResultEvent
  ): Next<IdentifierSearchResultState, IdentifierSearchResultEffect> {
    return when (event) {
      is ViewPatient -> dispatch(OpenPatientSummary(event.patientUuid))
      SearchPatient -> dispatch(OpenPatientSearch)
      is ShortCodeSearchCompleted -> displaySearchResults(event, model)
    }
  }

  private fun displaySearchResults(
      event: ShortCodeSearchCompleted,
      model: IdentifierSearchResultState
  ): Next<IdentifierSearchResultState, IdentifierSearchResultEffect> {
    val results = event.results

    val updatedModel = if (results.hasNoResults)
      model.noMatchingPatients()
    else
      model.patientsFetched(results)

    return next(updatedModel)
  }
}
