package org.simple.clinic.drugs.selectionv2

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ProtocolDrugSelected(val drugName: String, val prescribedDrug: PrescribedDrug?) : UiEvent

data class PrescribedDrugsScreenCreated(val patientUuid: UUID) : UiEvent
