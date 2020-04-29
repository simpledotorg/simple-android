package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent
import java.util.UUID


data class LinkIdWithPatientViewShown(val patientUuid: UUID, val identifier: Identifier) : UiEvent {
  override val analyticsName: String = "LinkIdWithPatient:Sheet Created"
}

object LinkIdWithPatientAddClicked : UiEvent {
  override val analyticsName = "LinkIdWithPatient:Add Clicked"
}

object LinkIdWithPatientCancelClicked : UiEvent {
  override val analyticsName = "LinkIdWithPatient:Cancel Clicked"
}

object LinkIdWithPatientLinked : UiEvent {
  override val analyticsName: String = "LinkIdWithPatient:Linked And Closed"
}

object LinkIdWithPatientCancelled : UiEvent {
  override val analyticsName: String = "LinkIdWithPatient:Cancelled And Closed"
}
