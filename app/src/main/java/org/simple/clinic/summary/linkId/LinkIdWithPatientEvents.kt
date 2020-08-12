package org.simple.clinic.summary.linkId

import org.simple.clinic.widgets.UiEvent

object LinkIdWithPatientLinked : UiEvent {
  override val analyticsName: String = "LinkIdWithPatient:Linked And Closed"
}

object LinkIdWithPatientCancelled : UiEvent {
  override val analyticsName: String = "LinkIdWithPatient:Cancelled And Closed"
}
