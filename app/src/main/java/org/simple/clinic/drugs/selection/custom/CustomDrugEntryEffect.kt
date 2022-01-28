package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency
import java.util.UUID

sealed class CustomDrugEntryEffect

data class SaveCustomDrugToPrescription(
    val patientUuid: UUID,
    val drugName: String,
    val dosage: String?,
    val rxNormCode: String?,
    val frequency: DrugFrequency?
) : CustomDrugEntryEffect()

data class UpdatePrescription(
    val patientUuid: UUID,
    val prescribedDrugUuid: UUID,
    val drugName: String,
    val dosage: String?,
    val rxNormCode: String?,
    val frequency: DrugFrequency?
) : CustomDrugEntryEffect()

object CloseSheetAndGoToEditMedicineScreen : CustomDrugEntryEffect()

data class FetchPrescription(val prescriptionUuid: UUID) : CustomDrugEntryEffect()

data class FetchDrug(val drugUuid: UUID) : CustomDrugEntryEffect()

data class RemoveDrugFromPrescription(val drugUuid: UUID) : CustomDrugEntryEffect()

object LoadDrugFrequencyChoiceItems : CustomDrugEntryEffect()

object HideKeyboard : CustomDrugEntryEffect()

object ShowKeyboard : CustomDrugEntryEffect()

object ClearFocusFromDosageEditText : CustomDrugEntryEffect()

data class SetCursorPosition(val position: Int) : CustomDrugEntryEffect()

sealed class CustomDrugEntryViewEffect : CustomDrugEntryEffect()

data class ShowEditFrequencyDialog(val frequency: DrugFrequency?) : CustomDrugEntryViewEffect()

data class SetDrugFrequency(val frequencyLabel: String) : CustomDrugEntryViewEffect()

data class SetDrugDosage(val dosage: String?) : CustomDrugEntryViewEffect()
