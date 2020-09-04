package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import java.util.UUID

interface UiActions {
  fun goBackToPreviousScreen()
  fun navigateToTeleconsultSuccessScreen(teleconsultRecordId: UUID)
}
