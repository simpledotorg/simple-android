package org.simple.clinic.scanid

import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

data class ScanSimpleIdScreenCameraPermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Scan Simple ID:Camera Permission:$result"
}
