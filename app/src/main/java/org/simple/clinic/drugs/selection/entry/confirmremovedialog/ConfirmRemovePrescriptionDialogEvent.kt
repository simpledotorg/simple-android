package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import org.simple.clinic.widgets.UiEvent

sealed class ConfirmRemovePrescriptionDialogEvent : UiEvent

object PrescriptionRemoved : ConfirmRemovePrescriptionDialogEvent()
