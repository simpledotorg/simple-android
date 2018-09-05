package org.simple.clinic.home.overdue

import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

class OverdueScreenCreated : UiEvent

data class CallPatientClicked(val phoneNumber: String) : UiEvent {
  override val analyticsName = "Overdue Screen:Call Patient Clicked"
}

data class CallPhonePermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Overdue Screen:Call Phone Permission:$result"
}
