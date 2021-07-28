package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.search.DrugFrequency
import java.util.UUID

sealed class CustomDrugEntryEffect

data class ShowEditFrequencyDialog(val frequency: DrugFrequency) : CustomDrugEntryEffect()

data class SaveCustomDrugToPrescription(
    val patientUuid: UUID,
    val drugName: String,
    val dosage: String?,
    val rxNormCode: String?,
    val frequency: DrugFrequency?
) : CustomDrugEntryEffect()

object CloseBottomSheet : CustomDrugEntryEffect()

data class FetchPrescription(val prescriptionUuid: UUID) : CustomDrugEntryEffect()

data class RemoveDrugFromPrescription(val drugUuid: UUID) : CustomDrugEntryEffect()

data class UpdatePrescription(
    val patientUuid: UUID,
    val prescribedDrugUuid: UUID,
    val drugName: String,
    val dosage: String?,
    val rxNormCode: String?,
    val frequency: DrugFrequency?
) : CustomDrugEntryEffect()
