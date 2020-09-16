package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import java.util.UUID

interface TeleconsultMedicinesUiActions {
  fun openEditMedicines(patientUuid: UUID)
  fun openDrugDurationSheet(prescription: PrescribedDrug)
  fun openDrugFrequencySheet(prescription: PrescribedDrug)
}
