package org.simple.clinic.drugs.selection.entry

import java.util.UUID

interface CustomPrescriptionEntryUiActions {
  fun finish()
  fun setMedicineName(name: String)
  fun setDosage(dosage: String?)
  fun showConfirmRemoveMedicineDialog(prescribedDrugUuid: UUID)
}
