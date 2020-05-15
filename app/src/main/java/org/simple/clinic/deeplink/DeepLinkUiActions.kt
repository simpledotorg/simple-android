package org.simple.clinic.deeplink

import java.util.UUID

interface DeepLinkUiActions {
  fun navigateToSetupActivity()
  fun navigateToMainActivity()
  fun navigateToPatientSummary(patientUuid: UUID)
  fun showPatientDoesNotExist()
}
