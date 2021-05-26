package org.simple.clinic.drugs.selection.entry

import java.util.UUID

sealed class CustomPrescriptionEntryEffect

data class SaveCustomPrescription(
    val patientUuid: UUID,
    val drugName: String,
    val dosage: String?
) : CustomPrescriptionEntryEffect()

data class UpdatePrescription(
    val patientUuid: UUID,
    val prescriptionUuid: UUID,
    val drugName: String,
    val dosage: String?
) : CustomPrescriptionEntryEffect()

object CloseSheet : CustomPrescriptionEntryEffect()

data class FetchPrescription(val prescriptionUuid: UUID) : CustomPrescriptionEntryEffect()

data class SetMedicineName(val drugName: String) : CustomPrescriptionEntryEffect()

data class SetDosage(val dosage: String?) : CustomPrescriptionEntryEffect()

data class ShowConfirmRemoveMedicineDialog(val prescriptionUuid: UUID) : CustomPrescriptionEntryEffect()
