package org.simple.clinic.qrscan

import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

data class CameraPermissionChanged(val result: RuntimePermissionResult) : UiEvent

class AadhaarScanClicked : UiEvent

data class QrScanned(val qrCode: String) : UiEvent
