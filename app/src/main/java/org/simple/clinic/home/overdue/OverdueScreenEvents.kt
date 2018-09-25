package org.simple.clinic.home.overdue

import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class OverdueScreenCreated : UiEvent

data class CallPatientClicked(val phoneNumber: String) : UiEvent {
  override val analyticsName = "Overdue Screen:Call Patient Clicked"
}

data class CallPhonePermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Overdue Screen:Call Phone Permission:$result"
}

data class RemindToCallLaterClicked(val appointmentUUID: UUID) : UiEvent {
  override val analyticsName = "Overdue Screen:Remind To Call Later clicked"
}

data class AgreedToVisitClicked(val appointmentUUID: UUID) : UiEvent {
  override val analyticsName = "Overdue Screen:Appointment marked as patient-agreed-to-visit"
}

object RemoveFromListClicked : UiEvent
