package org.simple.clinic.drugs.selection.entry

import java.util.UUID

sealed class CustomPrescriptionEntryEffect

data class SaveCustomPrescription(val patientUuid: UUID, val drugName: String, val dosage: String?) : CustomPrescriptionEntryEffect()

object CloseSheet : CustomPrescriptionEntryEffect()
