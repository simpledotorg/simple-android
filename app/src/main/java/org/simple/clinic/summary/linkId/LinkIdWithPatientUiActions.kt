package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier

interface LinkIdWithPatientUiActions {
  fun renderIdentifierText(identifier: Identifier)
}
