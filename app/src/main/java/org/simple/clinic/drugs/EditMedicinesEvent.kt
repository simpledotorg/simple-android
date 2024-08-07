package org.simple.clinic.drugs

import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.widgets.UiEvent

sealed class EditMedicinesEvent : UiEvent

data object AddNewPrescriptionClicked : EditMedicinesEvent() {
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

data object PrescribedDrugsDoneClicked : EditMedicinesEvent() {
  override val analyticsName = "Drugs:Protocol:Save Clicked"
}

data object PresribedDrugsRefillClicked : EditMedicinesEvent()

data class DrugsListFetched(
    val protocolDrugs: List<ProtocolDrugAndDosages>,
    val prescribedDrugs: List<PrescribedDrug>
) : EditMedicinesEvent()

data object PrescribedMedicinesRefilled : EditMedicinesEvent()

data class DrugFrequencyChoiceItemsLoaded(
    val drugFrequencyToLabelMap: Map<DrugFrequency?, DrugFrequencyLabel>
) : EditMedicinesEvent()
