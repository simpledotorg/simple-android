package org.simple.clinic.deeplink

import java.util.UUID

interface DeepLinkUiActions {
  fun navigateToSetupActivity()
  fun navigateToPatientSummary(patientUuid: UUID)
  fun showPatientDoesNotExist()
  fun showNoPatientUuidError()
}
