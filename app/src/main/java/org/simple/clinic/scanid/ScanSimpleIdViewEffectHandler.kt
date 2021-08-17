package org.simple.clinic.scanid

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.simple.clinic.mobius.ViewEffectsHandler

class ScanSimpleIdViewEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: ScanSimpleIdUiActions
): ViewEffectsHandler<ScanSimpleIdViewEffect> {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ScanSimpleIdUiActions): ScanSimpleIdViewEffectHandler
  }

  override fun handle(viewEffect: ScanSimpleIdViewEffect) {
    when(viewEffect) {
      ShowQrCodeScannerView -> uiActions.showQrCodeScannerView()
      HideQrCodeScannerView -> uiActions.hideQrCodeScannerView()
      HideEnteredCodeValidationError -> uiActions.hideEnteredCodeValidationError()
      is ShowEnteredCodeValidationError -> uiActions.showEnteredCodeValidationError(viewEffect.failure)
      is OpenPatientSummary -> uiActions.openPatientSummary(viewEffect.patientId)
      is OpenPatientSearch -> openPatientSearch(viewEffect)
    }
  }

  private fun openPatientSearch(openPatientSearch: OpenPatientSearch) {
    uiActions.openPatientSearch(
        openPatientSearch.additionalIdentifier,
        openPatientSearch.initialSearchQuery,
        openPatientSearch.patientPrefillInfo
    )
  }
}
