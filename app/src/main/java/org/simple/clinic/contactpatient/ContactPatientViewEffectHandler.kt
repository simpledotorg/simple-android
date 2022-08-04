package org.simple.clinic.contactpatient

import org.simple.clinic.mobius.ViewEffectsHandler

class ContactPatientViewEffectHandler(
    private val uiActions: ContactPatientUiActions
) : ViewEffectsHandler<ContactPatientViewEffect> {
  override fun handle(viewEffect: ContactPatientViewEffect) {
    // Nothing to see here, yet.
  }
}
