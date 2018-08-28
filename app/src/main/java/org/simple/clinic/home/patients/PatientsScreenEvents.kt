package org.simple.clinic.home.patients

import org.simple.clinic.widgets.UiEvent

class NewPatientClicked : UiEvent {
  override val analyticsName = "Home:Search For Patient Clicked"
}

class ScanAadhaarClicked : UiEvent {
  override val analyticsName = "Home:Scan For Aadhaar Clicked"
}

class UserApprovedStatusDismissed : UiEvent {
  override val analyticsName = "Home:Dismissed User Approved Status"
}
