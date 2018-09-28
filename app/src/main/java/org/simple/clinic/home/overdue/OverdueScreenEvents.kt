package org.simple.clinic.home.overdue

import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class OverdueScreenCreated : UiEvent

data class CallPatientClicked(val phoneNumber: String) : UiEvent {
  override val analyticsName = "Overdue Screen:Call Patient clicked"
}

data class CallPhonePermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Overdue Screen:Call Phone Permission:$result"
}

data class AgreedToVisitClicked(val appointmentUUID: UUID) : UiEvent {
  override val analyticsName = "Overdue Screen:Mark patient as 'agreed to visit' clicked"
}

data class RemindToCallLaterClicked(val appointmentUUID: UUID) : UiEvent {
  override val analyticsName = "Overdue Screen:Remind To Call Later clicked"
}

data class RemoveFromListClicked(val appointmentUUID: UUID) : UiEvent {
  override val analyticsName = "Overdue Screen:Remove From List clicked"
}

data class AppointmentExpanded(val patientUuid: UUID): UiEvent
