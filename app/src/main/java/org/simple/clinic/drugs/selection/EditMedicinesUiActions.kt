package org.simple.clinic.drugs.selection

import org.simple.clinic.drugs.PrescribedDrug
import java.util.UUID

interface EditMedicinesUiActions {
  fun showNewPrescriptionEntrySheet(patientUuid: UUID)
  fun showDosageSelectionSheet(drugName: String, patientUuid: UUID, prescribedDrugUuid: UUID?)
  fun showUpdateCustomPrescriptionSheet(prescribedDrug: PrescribedDrug)
  fun goBackToPatientSummary()
}
