package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import java.util.UUID

sealed class ConfirmRemovePrescriptionDialogEffect

object CloseDialog : ConfirmRemovePrescriptionDialogEffect()

data class RemovePrescription(val prescriptionUuid: UUID) : ConfirmRemovePrescriptionDialogEffect()
