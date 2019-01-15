package org.simple.clinic.drugs.selectionv2.dosage

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PrescribedDrugsWithDosagesSheetCreated(val drugName: String, val patientUuid: UUID) : UiEvent

data class DosageItemClicked(val dosage: DosageType) : UiEvent

data class DosageSelected(val dosage: String) : UiEvent

object NoneSelected : UiEvent

object ScreenDestroyed : UiEvent
