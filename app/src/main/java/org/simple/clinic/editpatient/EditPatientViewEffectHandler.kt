package org.simple.clinic.editpatient

import org.simple.clinic.mobius.ViewEffectsHandler

class EditPatientViewEffectHandler(
    private val ui: EditPatientUi
) : ViewEffectsHandler<EditPatientViewEffect> {

  override fun handle(viewEffect: EditPatientViewEffect) {
    when (viewEffect) {
      is HideValidationErrorsEffect -> ui.hideValidationErrors(viewEffect.validationErrors)
      is ShowValidationErrorsEffect -> showValidationErrors(viewEffect.validationErrors)
      ShowDatePatternInDateOfBirthLabelEffect -> ui.showDatePatternInDateOfBirthLabel()
      HideDatePatternInDateOfBirthLabelEffect -> ui.hideDatePatternInDateOfBirthLabel()
      GoBackEffect -> ui.goBack()
      ShowDiscardChangesAlertEffect -> ui.showDiscardChangesAlert()
    }
  }

  private fun showValidationErrors(validationErrors: Set<EditPatientValidationError>) {
    with(ui) {
      showValidationErrors(validationErrors)
      scrollToFirstFieldWithError()
    }
  }
}
