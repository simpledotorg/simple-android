package org.simple.clinic.summary.linkId

import org.simple.clinic.widgets.UiEvent

sealed class LinkIdWithPatientEvent : UiEvent

data object LinkIdWithPatientCancelClicked : LinkIdWithPatientEvent() {
  override val analyticsName = "LinkIdWithPatient:Cancel Clicked"
}

data object IdentifierAddedToPatient : LinkIdWithPatientEvent()

data object LinkIdWithPatientAddClicked : LinkIdWithPatientEvent() {
  override val analyticsName = "LinkIdWithPatient:Add Clicked"
}

data class PatientNameReceived(val patientName: String) : LinkIdWithPatientEvent()
