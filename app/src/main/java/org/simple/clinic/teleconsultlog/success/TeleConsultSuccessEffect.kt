package org.simple.clinic.teleconsultlog.success

import java.util.UUID

sealed class TeleConsultSuccessEffect {

  data class LoadPatientDetails(val patientUuid: UUID) : TeleConsultSuccessEffect()

  data object GoToHomeScreen : TeleConsultSuccessEffect()

  data class GoToPrescriptionScreen(
      val patientUuid: UUID,
      val teleconsultRecordId: UUID
  ) : TeleConsultSuccessEffect()
}
