package org.simple.clinic.search

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class PatientSearchUiRenderer(
    private val ui: PatientSearchUi
) : ViewRenderer<PatientSearchModel> {

  private val validationErrorsChangedCallback = ValueChangedCallback<Set<PatientSearchValidationError>>()

  private val searchQueryChangedCallback = ValueChangedCallback<String>()

  override fun render(model: PatientSearchModel) {
    validationErrorsChangedCallback.pass(model.validationErrors) { errors ->
      ui.setEmptyTextFieldErrorVisible(visible = errors.isNotEmpty())
    }

    searchQueryChangedCallback.pass(model.enteredQuery) { searchQuery ->
      if (searchQuery.isNotBlank()) {
        ui.hideAllPatientsInFacility()
        ui.showSearchButton()
      } else {
        ui.showAllPatientsInFacility()
        ui.hideSearchButton()
      }
    }
  }
}
