package org.simple.clinic.scanid.scannedqrcode

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId

class ScannedQrCodeUiRenderer(private val ui: ScannedQrCodeUi) :ViewRenderer<ScannedQrCodeModel>{
  override fun render(model: ScannedQrCodeModel) {
    if (model.identifier.type == IndiaNationalHealthId){
      ui.showIndianNationalHealthIdValue()
    } else {
      ui.showBpPassportValue()
    }
  }
}
