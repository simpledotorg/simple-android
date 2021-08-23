package org.simple.clinic.home.patients

import org.simple.clinic.mobius.ViewEffectsHandler

class PatientsTabViewEffectHandler(
    private val uiActions: PatientsTabUiActions
) : ViewEffectsHandler<PatientsTabViewEffect> {

  override fun handle(viewEffect: PatientsTabViewEffect) {
    when (viewEffect) {
      OpenEnterOtpScreen -> uiActions.openEnterCodeManuallyScreen()
      is OpenPatientSearchScreen -> uiActions.openPatientSearchScreen(viewEffect.additionalIdentifier)
      ShowUserWasApproved -> uiActions.showUserStatusAsApproved()
      HideUserAccountStatus -> uiActions.hideUserAccountStatus()
      OpenScanBpPassportScreen -> uiActions.openScanSimpleIdCardScreen()
      OpenTrainingVideo -> uiActions.openYouTubeLinkForSimpleVideo()
      ShowAppUpdateAvailable -> uiActions.showAppUpdateDialog()
    }
  }
}
