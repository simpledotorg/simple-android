package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class LinkIdWithPatientEvent : UiEvent

data class LinkIdWithPatientViewShown(val patientUuid: UUID, val identifier: Identifier) : LinkIdWithPatientEvent() {
  override val analyticsName: String = "LinkIdWithPatient:Sheet Created"
}

object LinkIdWithPatientCancelClicked : LinkIdWithPatientEvent() {
  override val analyticsName = "LinkIdWithPatient:Cancel Clicked"
}

data class CurrentUserLoaded(val user: User) : LinkIdWithPatientEvent()

object IdentifierAddedToPatient : LinkIdWithPatientEvent()
