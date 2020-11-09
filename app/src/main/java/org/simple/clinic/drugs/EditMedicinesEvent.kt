package org.simple.clinic.drugs

import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.widgets.UiEvent

sealed class EditMedicinesEvent : UiEvent

object AddNewPrescriptionClicked : EditMedicinesEvent() {
  override val analyticsName = "Drugs:Protocol:Add Custom Clicked"
}

data class ProtocolDrugClicked(
    val drugName: String,
    val prescription: PrescribedDrug?
) : EditMedicinesEvent() {
  override val analyticsName = "Drugs:Protocol:Selected"
}

data class CustomPrescriptionClicked(val prescribedDrug: PrescribedDrug) : EditMedicinesEvent() {
  override val analyticsName = "Drugs:Protocol:Edit CustomPrescription Clicked"
}

object PrescribedDrugsDoneClicked : EditMedicinesEvent() {
  override val analyticsName = "Drugs:Protocol:Save Clicked"
}

object PresribedDrugsRefillClicked : EditMedicinesEvent()

data class DrugsListFetched(
    val protocolDrugs: List<ProtocolDrugAndDosages>,
    val prescribedDrugs: List<PrescribedDrug>
) : EditMedicinesEvent()

object PrescribedMedicinesRefilled : EditMedicinesEvent()
