package org.simple.clinic.teleconsultlog.success

import org.simple.clinic.patient.Patient
import java.util.UUID

sealed class TeleConsultSuccessEffect {

  data class LoadPatientDetails(val patientUuid: UUID) : TeleConsultSuccessEffect()

  object GoToHomeScreen : TeleConsultSuccessEffect()

  data class GoToPrescriptionScreen(val patient: Patient) : TeleConsultSuccessEffect()
}
