package org.simple.clinic.drugs.selectionv2.dosage

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class DosagePickerSheetCreated(val drugName: String, val patientUuid: UUID) : UiEvent

data class DosageItemClicked(val dosage: DosageOption) : UiEvent
