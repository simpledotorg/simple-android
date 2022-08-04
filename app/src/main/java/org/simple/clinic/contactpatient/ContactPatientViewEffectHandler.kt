package org.simple.clinic.contactpatient

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.phone.Dialer
import org.simple.clinic.util.exhaustive

class ContactPatientViewEffectHandler(
    private val uiActions: ContactPatientUiActions
) : ViewEffectsHandler<ContactPatientViewEffect> {
  override fun handle(viewEffect: ContactPatientViewEffect) {
    when (viewEffect) {
      is DirectCallWithAutomaticDialer -> uiActions.directlyCallPatient(viewEffect.patientPhoneNumber, Dialer.Automatic)
      is DirectCallWithManualDialer -> uiActions.directlyCallPatient(viewEffect.patientPhoneNumber, Dialer.Manual)
    }.exhaustive()
  }
}
