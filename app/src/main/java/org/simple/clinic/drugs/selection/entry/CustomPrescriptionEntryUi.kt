package org.simple.clinic.drugs.selection.entry

import java.util.UUID

interface CustomPrescriptionEntryUi {
  fun setSaveButtonEnabled(canBeSaved: Boolean)
  fun finish()
  fun setDrugDosageText(dosagePlaceholder: String)
  fun moveDrugDosageCursorToBeginning()
  fun showEnterNewPrescriptionTitle()
  fun showEditPrescriptionTitle()
  fun hideRemoveButton()
  fun showRemoveButton()
  fun setMedicineName(name: String)
  fun setDosage(dosage: String?)
  fun showConfirmRemoveMedicineDialog(prescribedDrugUuid: UUID)
}
