package org.simple.clinic.drugs.selection.dosage

import java.util.UUID

sealed class DosagePickerEffect

data class LoadProtocolDrugsByName(val drugName: String) : DosagePickerEffect()

data class DeleteExistingPrescription(val prescriptionUuid: UUID): DosagePickerEffect()

object CloseScreen : DosagePickerEffect()
