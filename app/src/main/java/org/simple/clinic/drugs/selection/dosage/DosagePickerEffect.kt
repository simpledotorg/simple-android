package org.simple.clinic.drugs.selection.dosage

import org.simple.clinic.protocol.ProtocolDrug
import java.util.UUID

sealed class DosagePickerEffect

data class LoadProtocolDrugsByName(val drugName: String) : DosagePickerEffect()

data class DeleteExistingPrescription(val prescriptionUuid: UUID) : DosagePickerEffect()

object CloseScreen : DosagePickerEffect()

data class CreateNewPrescription(
    val patientUuid: UUID,
    val protocolDrug: ProtocolDrug
) : DosagePickerEffect()

data class ChangeExistingPrescription(
    val patientUuid: UUID,
    val prescriptionUuid: UUID,
    val protocolDrug: ProtocolDrug
) : DosagePickerEffect()
