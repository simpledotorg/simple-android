package org.simple.clinic.editpatient.deletepatient

import org.simple.clinic.patient.DeletedReason

interface UiActions {
  fun showConfirmDeleteDialog(patientName: String, deletedReason: DeletedReason)
  fun showConfirmDiedDialog(patientName: String)
  fun showHomeScreen()
}
