package org.simple.clinic.drugs.selection

import java.util.UUID

interface EditMedicinesUiActions {
  fun showNewPrescriptionEntrySheet(patientUuid: UUID)
  fun showDosageSelectionSheet(drugName: String, patientUuid: UUID, prescribedDrugUuid: UUID?)
}
