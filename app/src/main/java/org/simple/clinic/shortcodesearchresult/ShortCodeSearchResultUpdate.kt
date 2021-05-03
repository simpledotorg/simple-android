package org.simple.clinic.shortcodesearchresult

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class ShortCodeSearchResultUpdate : Update<IdentifierSearchResultState, IdentifierSearchResultEvent, ShortCodeSearchResultEffect> {

  override fun update(
      model: IdentifierSearchResultState,
      event: IdentifierSearchResultEvent
  ): Next<IdentifierSearchResultState, ShortCodeSearchResultEffect> {
    return when (event) {
      is ViewPatient -> dispatch(OpenPatientSummary(event.patientUuid))
      SearchPatient -> dispatch(OpenPatientSearch)
      is ShortCodeSearchCompleted -> displaySearchResults(event, model)
    }
  }

  private fun displaySearchResults(
      event: ShortCodeSearchCompleted,
      model: IdentifierSearchResultState
  ): Next<IdentifierSearchResultState, ShortCodeSearchResultEffect> {
    val results = event.results

    val updatedModel = if (results.hasNoResults)
      model.noMatchingPatients()
    else
      model.patientsFetched(results)

    return next(updatedModel)
  }
}
