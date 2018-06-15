package org.simple.clinic.drugs

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ProtocolDrugDosageSelected(val drugName: String, val dosage: String) : UiEvent

data class PrescribedDrugsEntryScreenCreated(val patientUuid: UUID) : UiEvent
