package org.simple.clinic.scanid

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ScanSimpleIdScreenQrCodeScanned(val text: String) : UiEvent {
  override val analyticsName = "Scan Simple Card:QR code scanned"
}

sealed class ScanSimpleIdScreenPassportCodeScanned : UiEvent {

  data class ValidPassportCode(val bpPassportUuid: UUID) : ScanSimpleIdScreenPassportCodeScanned() {
    override val analyticsName = "Scan Simple Card:Valid BP passport code scanned"
  }

  object InvalidPassportCode : ScanSimpleIdScreenPassportCodeScanned() {
    override val analyticsName = "Scan Simple Card:Invalid BP passport code scanned"
  }
}
