package org.simple.clinic.home.overdue.phonemask

import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PhoneMaskBottomSheetCreated(val patientUuid: UUID) : UiEvent

object NormalCallClicked : UiEvent {
  override val analyticsName = "Phone mask bottom sheet:Normal call clicked"
}

object SecureCallClicked : UiEvent {
  override val analyticsName = "Phone mask bottom sheet:Secure call clicked"
}

data class CallPhonePermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Phone mask bottom sheet:Call Phone Permission:$result"
}
