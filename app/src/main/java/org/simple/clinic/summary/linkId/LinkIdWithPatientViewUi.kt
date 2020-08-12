package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier

interface LinkIdWithPatientViewUi : LinkIdWithPatientUiActions {
  fun renderIdentifierText(identifier: Identifier)
  fun closeSheetWithIdLinked()
  fun closeSheetWithoutIdLinked()
}
