package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.util.exhaustive

class PatientEntryViewEffectHandler(
    private val uiActions: PatientEntryUiActions
) : ViewEffectsHandler<PatientEntryViewEffect> {

  private val showDatePatternInLabelValueChangedCallback = ValueChangedCallback<Boolean>()

  override fun handle(viewEffect: PatientEntryViewEffect) {
    when (viewEffect) {
      is PrefillFields -> uiActions.prefillFields(viewEffect.patientEntry)
      ScrollFormOnGenderSelection -> uiActions.scrollFormOnGenderSelection()
      is ShowDatePatternInDateOfBirthLabel -> showDatePatternInLabelValueChangedCallback.pass(viewEffect.show,
          uiActions::setShowDatePatternInDateOfBirthLabel)
    }.exhaustive()
  }
}
