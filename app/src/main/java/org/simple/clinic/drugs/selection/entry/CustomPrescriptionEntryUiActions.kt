package org.simple.clinic.drugs.selection.entry

interface CustomPrescriptionEntryUiActions {
  fun finish()
  fun setMedicineName(name: String)
  fun setDosage(dosage: String?)
}
