package org.simple.clinic.drugs

import org.simple.clinic.widgets.UiEvent

sealed class EditMedicinesEvent : UiEvent

object AddNewPrescriptionClicked : EditMedicinesEvent() {
  override val analyticsName = "Drugs:Protocol:Add Custom Clicked"
}
