package org.simple.clinic.scanid.scannedqrcode

import org.simple.clinic.widgets.UiEvent

sealed class ScannedQrCodeEvent : UiEvent

data object NewOngoingPatientEntrySaved : ScannedQrCodeEvent()

data object RegisterNewPatientClicked : ScannedQrCodeEvent() {
  override val analyticsName = "Blank BP passport sheet:Register new patient"
}

data object AddToExistingPatientClicked : ScannedQrCodeEvent() {
  override val analyticsName = "Blank BP passport sheet:Add to existing patient"
}
