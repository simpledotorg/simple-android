package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.OpenIntention
import org.simple.clinic.drugs.PrescribedDrug
import java.util.UUID

interface TeleconsultMedicinesUiActions {
  fun openEditMedicines(patientUuid: UUID, openIntention: OpenIntention)
  fun openDrugDurationSheet(prescription: PrescribedDrug)
  fun openDrugFrequencySheet(prescription: PrescribedDrug)
}
