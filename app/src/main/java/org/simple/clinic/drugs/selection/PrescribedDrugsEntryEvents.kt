package org.simple.clinic.drugs.selection

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ProtocolDrugDosageSelected constructor(val drug: ProtocolDrug, val dosage: String) : UiEvent

data class ProtocolDrugDosageUnselected constructor(val drug: ProtocolDrug, val prescription: PrescribedDrug) : UiEvent

data class PrescribedDrugsEntryScreenCreated(val patientUuid: UUID) : UiEvent

class AddNewPrescriptionClicked : UiEvent
