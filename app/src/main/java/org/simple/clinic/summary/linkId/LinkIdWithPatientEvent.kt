package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class LinkIdWithPatientEvent : UiEvent

data class LinkIdWithPatientViewShown(val patientUuid: UUID, val identifier: Identifier) : LinkIdWithPatientEvent() {
  override val analyticsName: String = "LinkIdWithPatient:Sheet Created"
}

object LinkIdWithPatientCancelClicked : LinkIdWithPatientEvent() {
  override val analyticsName = "LinkIdWithPatient:Cancel Clicked"
}

object IdentifierAddedToPatient : LinkIdWithPatientEvent()

object LinkIdWithPatientAddClicked : LinkIdWithPatientEvent() {
  override val analyticsName = "LinkIdWithPatient:Add Clicked"
}

data class PatientNameReceived(val patientName: String) : LinkIdWithPatientEvent()
