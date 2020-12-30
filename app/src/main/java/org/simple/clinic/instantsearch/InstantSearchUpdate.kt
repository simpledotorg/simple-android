package org.simple.clinic.instantsearch

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier

class InstantSearchUpdate : Update<InstantSearchModel, InstantSearchEvent, InstantSearchEffect> {

  /**
   * Regular expression that matches digits with interleaved white spaces
   **/
  private val digitsRegex = Regex("[\\s*\\d+]+")

  override fun update(model: InstantSearchModel, event: InstantSearchEvent): Next<InstantSearchModel, InstantSearchEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> next(model.facilityLoaded(event.facility), LoadAllPatients(event.facility))
      is AllPatientsLoaded -> allPatientsLoaded(model, event)
      is SearchResultsLoaded -> searchResultsLoaded(model, event)
      is SearchQueryValidated -> searchQueryValidated(model, event)
      is SearchResultClicked -> searchResultClicked(event)
    }
  }

  private fun searchResultClicked(event: SearchResultClicked): Next<InstantSearchModel, InstantSearchEffect> {
    return dispatch(OpenPatientSummary(event.patientId))
  }

  private fun searchQueryValidated(model: InstantSearchModel, event: SearchQueryValidated): Next<InstantSearchModel, InstantSearchEffect> {
    return when (val validationResult = event.result) {
      is InstantSearchValidator.Result.Valid -> {
        val criteria = searchCriteriaFromInput(validationResult.searchQuery, model.additionalIdentifier)
        dispatch(SearchWithCriteria(criteria, model.facility!!))
      }
      InstantSearchValidator.Result.LengthTooShort -> noChange()
      InstantSearchValidator.Result.Empty -> dispatch(LoadAllPatients(model.facility!!))
    }
  }

  private fun searchCriteriaFromInput(
      inputString: String,
      additionalIdentifier: Identifier?
  ): PatientSearchCriteria {
    return when {
      digitsRegex.matches(inputString) -> PatientSearchCriteria.PhoneNumber(inputString.filterNot { it.isWhitespace() }, additionalIdentifier)
      else -> PatientSearchCriteria.Name(inputString, additionalIdentifier)
    }
  }

  private fun searchResultsLoaded(model: InstantSearchModel, event: SearchResultsLoaded): Next<InstantSearchModel, InstantSearchEffect> {
    if (!model.hasSearchQuery) return noChange()

    return dispatch(ShowPatientSearchResults(event.patientsSearchResults, model.facility!!))
  }

  private fun allPatientsLoaded(model: InstantSearchModel, event: AllPatientsLoaded): Next<InstantSearchModel, InstantSearchEffect> {
    if (model.hasSearchQuery) return noChange()

    return dispatch(ShowPatientSearchResults(event.patients, model.facility!!))
  }
}
