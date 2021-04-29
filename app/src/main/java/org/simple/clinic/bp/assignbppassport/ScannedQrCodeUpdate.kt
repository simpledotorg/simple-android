package org.simple.clinic.bp.assignbppassport

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.patient.OngoingNewPatientEntry

class ScannedQrCodeUpdate : Update<ScannedQrCodeModel, ScannedQrCodeEvent, ScannedQrCodeEffect> {
  override fun update(model: ScannedQrCodeModel, event: ScannedQrCodeEvent): Next<ScannedQrCodeModel, ScannedQrCodeEffect> {
    return when (event) {
      RegisterNewPatientClicked -> {
        val ongoingNewPatientEntry = OngoingNewPatientEntry(identifier = model.identifier)
        dispatch(SaveNewOngoingPatientEntry(ongoingNewPatientEntry))
      }
      NewOngoingPatientEntrySaved -> dispatch(SendBlankScannedQrCodeResult(RegisterNewPatient))
      is AddToExistingPatientClicked -> dispatch(SendBlankScannedQrCodeResult(AddToExistingPatient))
    }
  }
}
