package org.simple.clinic.drugs.selection

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

object PrescribedDrugsDoneClicked : UiEvent {
  override val analyticsName = "Drugs:Protocol:Save Clicked"
}
