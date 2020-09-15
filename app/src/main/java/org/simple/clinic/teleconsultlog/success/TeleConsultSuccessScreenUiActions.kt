package org.simple.clinic.teleconsultlog.success

import java.util.UUID

interface TeleConsultSuccessScreenUiActions {
  fun goToHomeScreen()
  fun goToPrescriptionScreen(patientUuid: UUID, teleconsultRecordId: UUID)
}
