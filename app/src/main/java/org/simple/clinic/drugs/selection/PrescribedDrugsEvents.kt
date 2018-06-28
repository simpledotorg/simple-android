package org.simple.clinic.drugs.selection

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ProtocolDrugDosageSelected(val drug: ProtocolDrug, val dosage: String) : UiEvent

data class ProtocolDrugDosageUnselected constructor(val drug: ProtocolDrug, val prescription: PrescribedDrug) : UiEvent

data class PrescribedDrugsScreenCreated(val patientUuid: UUID) : UiEvent

class AddNewPrescriptionClicked : UiEvent

class PrescribedDrugsDoneClicked : UiEvent

data class DeleteCustomPrescriptionClicked(val prescription: PrescribedDrug) : UiEvent
