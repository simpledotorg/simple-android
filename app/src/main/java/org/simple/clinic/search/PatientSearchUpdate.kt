package org.simple.clinic.search

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class PatientSearchUpdate : Update<PatientSearchModel, PatientSearchEvent, PatientSearchEffect> {

  override fun update(model: PatientSearchModel, event: PatientSearchEvent): Next<PatientSearchModel, PatientSearchEffect> {
    return when (event) {
      is SearchQueryTextChanged -> next(model.queryChanged(event.text))
      is SearchClicked -> {
        val validationErrors = validateInput(model.enteredQuery)

        if (validationErrors.isEmpty())
          noChange()
        else
          next(
              model.invalidQuery(validationErrors),
              ReportValidationErrorsToAnalytics(validationErrors) as PatientSearchEffect
          )
      }
    }
  }

  private fun validateInput(inputText: String): Set<PatientSearchValidationError> {
    return if (inputText.isBlank())
      setOf(PatientSearchValidationError.INPUT_EMPTY)
    else
      emptySet()
  }
}
