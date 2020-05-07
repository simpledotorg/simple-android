package org.simple.clinic.drugs.selection.entry

interface CustomPrescriptionEntryUi {
  fun setSaveButtonEnabled(canBeSaved: Boolean)
  fun setDrugDosageText(dosagePlaceholder: String)
  fun moveDrugDosageCursorToBeginning()
  fun showEnterNewPrescriptionTitle()
  fun showEditPrescriptionTitle()
  fun hideRemoveButton()
  fun showRemoveButton()
}
