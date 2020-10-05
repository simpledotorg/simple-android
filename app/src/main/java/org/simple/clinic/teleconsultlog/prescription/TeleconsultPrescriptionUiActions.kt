package org.simple.clinic.teleconsultlog.prescription

import java.util.UUID

interface TeleconsultPrescriptionUiActions {
  fun goBackToPreviousScreen()
  fun showSignatureRequiredError()
  fun openSharePrescriptionScreen(teleconsultRecordId: UUID, medicalInstructions: String)
  fun showMedicinesRequiredError()
}
