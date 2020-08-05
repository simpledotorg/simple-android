package org.simple.clinic.scanid

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface ScanSimpleIdUi : ScanSimpleIdUiActions {
  fun openAddIdToPatientScreen(identifier: Identifier)
}
