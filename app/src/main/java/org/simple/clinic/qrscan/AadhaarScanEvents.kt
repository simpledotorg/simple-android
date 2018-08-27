package org.simple.clinic.qrscan

import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

data class CameraPermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Aadhaar Scan:Camera Permission:$result "
}

class AadhaarScanClicked : UiEvent {
  override val analyticsName = "Aadhaar Scan:Scan Clicked"
}

data class QrScanned(val qrCode: String) : UiEvent {
  override val analyticsName = "Aadhaar Scan:Scanned QR code"
}
