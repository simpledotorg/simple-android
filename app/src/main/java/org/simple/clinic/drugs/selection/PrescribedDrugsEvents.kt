package org.simple.clinic.drugs.selection

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ProtocolDrugDosageSelected(val drug: ProtocolDrug, val dosage: String) : UiEvent {
  override val analyticsName = "Drugs:Protocol:Selected"
}

data class ProtocolDrugDosageUnselected(val drug: ProtocolDrug, val prescription: PrescribedDrug) : UiEvent {
  override val analyticsName = "Drugs:Protocol:Unselected"
}

data class PrescribedDrugsScreenCreated(val patientUuid: UUID) : UiEvent

class AddNewPrescriptionClicked : UiEvent {
  override val analyticsName = "Drugs:Protocol:Add Custom Clicked"
}

class PrescribedDrugsDoneClicked : UiEvent {
  override val analyticsName = "Drugs:Protocol:Save Clicked"
}

data class DeleteCustomPrescriptionClicked(val prescription: PrescribedDrug) : UiEvent {
  override val analyticsName = "Drugs:Protocol:Delete Custom Clicked"
}
