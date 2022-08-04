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
      is MaskedCallWithAutomaticDialer -> uiActions.maskedCallPatient(viewEffect.patientPhoneNumber, viewEffect.proxyPhoneNumber, Dialer.Automatic)
      is MaskedCallWithManualDialer -> uiActions.maskedCallPatient(viewEffect.patientPhoneNumber, viewEffect.proxyPhoneNumber, Dialer.Manual)
      CloseScreen -> uiActions.closeSheet()
    }.exhaustive()
  }
}
