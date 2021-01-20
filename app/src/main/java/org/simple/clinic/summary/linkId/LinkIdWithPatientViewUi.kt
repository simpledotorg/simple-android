package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier

interface LinkIdWithPatientViewUi {
  fun renderPatientName(patientName: String)
}
