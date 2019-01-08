package org.simple.clinic.drugs.selectionv2.dosage

import org.simple.clinic.widgets.UiEvent

data class PrescribedDrugsWithDosagesSheetCreated(val drugName: String) : UiEvent

object ScreenDestroyed: UiEvent
