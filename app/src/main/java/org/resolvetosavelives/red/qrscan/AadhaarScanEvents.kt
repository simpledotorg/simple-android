package org.resolvetosavelives.red.qrscan

import org.resolvetosavelives.red.util.RuntimePermissionResult
import org.resolvetosavelives.red.widgets.UiEvent

data class CameraPermissionChanged(val result: RuntimePermissionResult) : UiEvent

class AadhaarScanClicked : UiEvent

data class AadhaarScanned(val qrCode: String) : UiEvent
