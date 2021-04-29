package org.simple.clinic.bp.assignbppassport

import org.simple.clinic.widgets.UiEvent

sealed class ScannedQrCodeEvent : UiEvent

object NewOngoingPatientEntrySaved : ScannedQrCodeEvent()

object RegisterNewPatientClicked : ScannedQrCodeEvent() {
  override val analyticsName = "Blank BP passport sheet:Register new patient"
}

object AddToExistingPatientClicked : ScannedQrCodeEvent() {
  override val analyticsName = "Blank BP passport sheet:Add to existing patient"
}
