package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class CustomPrescriptionSheetCreated(val patientUuid: UUID) : UiEvent

data class CustomPrescriptionDrugNameTextChanged(val name: String) : UiEvent

data class CustomPrescriptionDrugDosageTextChanged(val dosage: String) : UiEvent

data class CustomPrescriptionDrugDosageFocusChanged(val hasFocus: Boolean) : UiEvent

class SaveCustomPrescriptionClicked : UiEvent
