package org.simple.clinic.search

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier

class PatientSearchUpdate : Update<PatientSearchModel, PatientSearchEvent, PatientSearchEffect> {

  /**
   * Regular expression that matches digits with interleaved white spaces
   **/
  private val digitsRegex = Regex("[\\s*\\d+]+")

  override fun update(model: PatientSearchModel, event: PatientSearchEvent): Next<PatientSearchModel, PatientSearchEffect> {
    return when (event) {
      is SearchQueryTextChanged -> next(model.queryChanged(event.text))
      is SearchClicked -> searchClicked(model)
      is PatientItemClicked -> dispatch(OpenPatientSummary(event.patientUuid))
    }
  }

  private fun searchClicked(model: PatientSearchModel): Next<PatientSearchModel, PatientSearchEffect> {
    val validationErrors = validateInput(model.enteredQuery)

    return if (validationErrors.isEmpty()) {
      val searchCriteria = searchCriteriaFromInput(model.enteredQuery, model.additionalIdentifier)

      dispatch(OpenSearchResults(searchCriteria) as PatientSearchEffect)
    } else {
      next(model.invalidQuery(validationErrors), ReportValidationErrorsToAnalytics(validationErrors) as PatientSearchEffect)
    }
  }

  private fun validateInput(inputText: String): Set<PatientSearchValidationError> {
    return if (inputText.isBlank())
      setOf(PatientSearchValidationError.INPUT_EMPTY)
    else
      emptySet()
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
}
