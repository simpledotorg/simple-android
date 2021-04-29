package org.simple.clinic.bp.assignbppassport

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId

class ScannedQrCodeUiRenderer(private val ui: ScannedQrCodeUi) :ViewRenderer<BpPassportModel>{
  override fun render(model: BpPassportModel) {
    if (model.identifier.type == IndiaNationalHealthId){
      ui.showIndianNationalHealthIdValue()
    } else {
      ui.showBpPassportValue()
    }
  }
}
