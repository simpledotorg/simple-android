package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.search.Drug
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class CustomDrugEntryEvent : UiEvent

data class DosageEdited(val dosage: String) : CustomDrugEntryEvent()

data class DosageFocusChanged(val hasFocus: Boolean) : CustomDrugEntryEvent()

object EditFrequencyClicked : CustomDrugEntryEvent()

data class FrequencyEdited(val frequency: DrugFrequency?) : CustomDrugEntryEvent()

data class AddMedicineButtonClicked(val patientUuid: UUID) : CustomDrugEntryEvent()

object CustomDrugSaved : CustomDrugEntryEvent()

data class PrescribedDrugFetched(val prescription: PrescribedDrug) : CustomDrugEntryEvent()

object ExistingDrugRemoved : CustomDrugEntryEvent()

object RemoveDrugButtonClicked : CustomDrugEntryEvent()

data class DrugFetched(val drug: Drug) : CustomDrugEntryEvent()

data class DrugFrequencyChoiceItemsLoaded(
    val drugFrequencyToLabelMap: Map<DrugFrequency?, DrugFrequencyLabel>
) : CustomDrugEntryEvent()

object ImeActionDoneClicked : CustomDrugEntryEvent()
