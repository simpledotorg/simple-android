package org.simple.clinic.drugs.selection.entry

import java.util.UUID

interface CustomPrescriptionEntryUi : CustomPrescriptionEntryUiActions {
  fun setSaveButtonEnabled(canBeSaved: Boolean)
  fun setDrugDosageText(dosagePlaceholder: String)
  fun moveDrugDosageCursorToBeginning()
  fun showEnterNewPrescriptionTitle()
  fun showEditPrescriptionTitle()
  fun hideRemoveButton()
  fun showRemoveButton()
  fun showConfirmRemoveMedicineDialog(prescribedDrugUuid: UUID)
}
