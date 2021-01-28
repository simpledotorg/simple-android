package org.simple.clinic.bp.assignbppassport

import org.simple.clinic.widgets.UiEvent

sealed class BpPassportEvent : UiEvent

object NewOngoingPatientEntrySaved : BpPassportEvent()

object RegisterNewPatientClicked : BpPassportEvent() {
  override val analyticsName = "Blank BP passport sheet:Register new patient"
}

object AddToExistingPatientClicked : BpPassportEvent() {
  override val analyticsName = "Blank BP passport sheet:Add to existing patient"
}
