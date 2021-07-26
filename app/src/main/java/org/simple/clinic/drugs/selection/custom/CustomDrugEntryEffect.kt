package org.simple.clinic.drugs.selection.custom

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
