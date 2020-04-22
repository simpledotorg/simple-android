package org.simple.clinic.drugs.selection

import java.util.UUID

interface EditMedicinesUiActions {
  fun showNewPrescriptionEntrySheet(patientUuid: UUID)
}
