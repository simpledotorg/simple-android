package org.simple.clinic.scanid.scannedqrcode

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class ScannedQrCodeViewEffectHandler(
    private val uiActions: ScannedQrCodeUiActions
) : ViewEffectsHandler<ScannedQrCodeViewEffect> {

  override fun handle(viewEffect: ScannedQrCodeViewEffect) {
    when (viewEffect) {
      is SendBlankScannedQrCodeResult -> uiActions.sendScannedQrCodeResult(viewEffect.scannedQRCodeResult)
    }.exhaustive()
  }
}
