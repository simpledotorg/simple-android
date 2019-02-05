package org.simple.clinic.home.patients

import org.simple.clinic.widgets.UiEvent

object NewPatientClicked : UiEvent {
  override val analyticsName = "Patients:Search For Patient Clicked"
}

class UserApprovedStatusDismissed : UiEvent {
  override val analyticsName = "Patients:Dismissed User Approved Status"
}

class PatientsEnterCodeManuallyClicked : UiEvent {
  override val analyticsName = "Patients:Enter Code Manually Clicked"
}

object ScanCardIdButtonClicked : UiEvent {
  override val analyticsName = "Patients:Scan Simple Card Clicked"
}
