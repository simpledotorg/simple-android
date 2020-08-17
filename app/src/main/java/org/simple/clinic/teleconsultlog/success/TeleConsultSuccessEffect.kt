package org.simple.clinic.teleconsultlog.success

import java.util.UUID

sealed class TeleConsultSuccessEffect {

  data class LoadPatientDetails(val patientUuid: UUID) : TeleConsultSuccessEffect()
}
