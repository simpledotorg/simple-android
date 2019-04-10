package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent
import java.util.UUID


data class LinkIdWithPatientSheetCreated(val patientUuid: UUID, val identifier: Identifier) : UiEvent

object LinkIdWithPatientAddClicked : UiEvent {
  override val analyticsName = "LinkIdWithPatient : Add Clicked"
}


object LinkIdWithPatientCancelClicked : UiEvent {
  override val analyticsName = "LinkIdWithPatient : Cancel Clicked"
}