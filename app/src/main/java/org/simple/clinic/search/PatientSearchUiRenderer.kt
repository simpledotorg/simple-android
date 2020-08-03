package org.simple.clinic.search

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class PatientSearchUiRenderer(
    private val ui: PatientSearchUi
) : ViewRenderer<PatientSearchModel> {

  private val validationErrorsChangedCallback = ValueChangedCallback<Set<PatientSearchValidationError>>()

  override fun render(model: PatientSearchModel) {
    validationErrorsChangedCallback.pass(model.validationErrors) { errors ->
      ui.setEmptyTextFieldErrorVisible(visible = errors.isNotEmpty())
    }
  }
}
