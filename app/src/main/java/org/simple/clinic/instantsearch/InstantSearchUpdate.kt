package org.simple.clinic.instantsearch

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.instantsearch.InstantSearchValidator.Result.Empty
import org.simple.clinic.instantsearch.InstantSearchValidator.Result.LengthTooShort
import org.simple.clinic.instantsearch.InstantSearchValidator.Result.Valid
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier

class InstantSearchUpdate : Update<InstantSearchModel, InstantSearchEvent, InstantSearchEffect> {

  /**
   * Regular expression that matches digits with interleaved white spaces
   **/
  private val digitsRegex = Regex("[\\s*\\d+]+")

  override fun update(model: InstantSearchModel, event: InstantSearchEvent): Next<InstantSearchModel, InstantSearchEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> next(
          model.facilityLoaded(event.facility)
              .loadingAllPatients(),
          LoadAllPatients(event.facility)
      )
      is AllPatientsLoaded -> allPatientsLoaded(model, event)
      is SearchResultsLoaded -> searchResultsLoaded(model, event)
      is SearchQueryValidated -> searchQueryValidated(model, event)
      is SearchResultClicked -> searchResultClicked(model, event)
      is SearchQueryChanged -> next(model.searchQueryChanged(event.searchQuery), ValidateSearchQuery(event.searchQuery))
      SavedNewOngoingPatientEntry -> dispatch(OpenPatientEntryScreen(model.facility!!))
      RegisterNewPatientClicked -> registerNewPatient(model)
    }
  }

  private fun registerNewPatient(model: InstantSearchModel): Next<InstantSearchModel, InstantSearchEffect> {
    var ongoingPatientEntry = when (val searchCriteria = searchCriteriaFromInput(model.searchQuery.orEmpty(), model.additionalIdentifier)) {
      is PatientSearchCriteria.Name -> OngoingNewPatientEntry.fromFullName(searchCriteria.patientName)
      is PatientSearchCriteria.PhoneNumber -> OngoingNewPatientEntry.fromPhoneNumber(searchCriteria.phoneNumber)
    }

    if (model.hasAdditionalIdentifier) {
      ongoingPatientEntry = ongoingPatientEntry.withIdentifier(model.additionalIdentifier!!)
    }

    return dispatch(SaveNewOngoingPatientEntry(ongoingPatientEntry))
  }

  private fun searchResultClicked(model: InstantSearchModel, event: SearchResultClicked): Next<InstantSearchModel, InstantSearchEffect> {
    val effect = if (model.hasAdditionalIdentifier)
      OpenLinkIdWithPatientScreen(event.patientId, model.additionalIdentifier!!)
    else
      OpenPatientSummary(event.patientId)

    return dispatch(effect)
  }

  private fun searchQueryValidated(model: InstantSearchModel, event: SearchQueryValidated): Next<InstantSearchModel, InstantSearchEffect> {
    return when (val validationResult = event.result) {
      is Valid -> {
        val criteria = searchCriteriaFromInput(validationResult.searchQuery, model.additionalIdentifier)
        dispatch(HideNoPatientsInFacility, HideNoSearchResults, SearchWithCriteria(criteria, model.facility!!))
      }
      LengthTooShort -> noChange()
      Empty -> next(
          model.loadingAllPatients(),
          HideNoPatientsInFacility,
          HideNoSearchResults,
          LoadAllPatients(model.facility!!)
      )
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

    val effect = if (event.patientsSearchResults.isNotEmpty())
      ShowPatientSearchResults(event.patientsSearchResults, model.facility!!)
    else
      ShowNoSearchResults

    return dispatch(effect)
  }

  private fun allPatientsLoaded(model: InstantSearchModel, event: AllPatientsLoaded): Next<InstantSearchModel, InstantSearchEffect> {
    if (model.hasSearchQuery) return noChange()

    val effect = if (event.patients.isNotEmpty())
      ShowPatientSearchResults(event.patients, model.facility!!)
    else
      ShowNoPatientsInFacility(model.facility!!)

    return next(model.allPatientsLoaded(), effect)
  }
}
