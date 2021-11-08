package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class PatientEntryViewEffectHandler(
    private val uiActions: PatientEntryUiActions
) : ViewEffectsHandler<PatientEntryViewEffect> {

  override fun handle(viewEffect: PatientEntryViewEffect) {
    when (viewEffect) {
      is PrefillFields -> uiActions.prefillFields(viewEffect.patientEntry)
      ScrollFormOnGenderSelection -> uiActions.scrollFormOnGenderSelection()
    }.exhaustive()
  }
}
