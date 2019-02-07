package org.simple.clinic.scanid

import org.simple.clinic.widgets.UiEvent

data class ScanSimpleIdScreenQrCodeScanned(val text: String) : UiEvent {
  override val analyticsName = "Scan Simple Card:QR code scanned"
}
