package org.simple.clinic.drugs.selectionv2

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ProtocolDrugClicked(val drugName: String, val prescriptionForProtocolDrug: PrescribedDrug?) : UiEvent

data class PrescribedDrugsScreenCreated(val patientUuid: UUID) : UiEvent

data class CustomPrescriptionClicked(val prescribedDrug: PrescribedDrug) : UiEvent