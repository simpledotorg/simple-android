package org.simple.clinic.scanid.scannedqrcode

import org.simple.clinic.mobius.ViewEffectsHandler

class ScannedQrCodeViewEffectHandler(
    private val uiActions: ScannedQrCodeUiActions
) : ViewEffectsHandler<ScannedQrCodeViewEffect> {

  override fun handle(viewEffect: ScannedQrCodeViewEffect) {
    // no-op
  }
}
