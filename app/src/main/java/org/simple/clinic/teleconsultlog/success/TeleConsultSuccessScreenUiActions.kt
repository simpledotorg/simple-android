package org.simple.clinic.teleconsultlog.success

import org.simple.clinic.patient.Patient

interface TeleConsultSuccessScreenUiActions {
  fun goToHomeScreen()
  fun goToPrescriptionScreen(patient: Patient)
}
