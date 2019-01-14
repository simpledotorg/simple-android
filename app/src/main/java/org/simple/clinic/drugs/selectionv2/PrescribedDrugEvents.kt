package org.simple.clinic.drugs.selectionv2

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ProtocolDrugSelected(val drugName: String) : UiEvent

data class PrescribedDrugsScreenCreated(val patientUuid: UUID) : UiEvent


