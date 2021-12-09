package org.simple.clinic.drugs

import org.simple.clinic.drugs.selection.EditMedicinesUiActions
import org.simple.clinic.mobius.ViewEffectsHandler

class EditMedicinesViewEffectHandler(
    private val uiActions: EditMedicinesUiActions
) : ViewEffectsHandler<EditMedicinesViewEffect> {
  override fun handle(viewEffect: EditMedicinesViewEffect) {
    when (viewEffect) {
      is ShowNewPrescriptionEntrySheet -> uiActions.showNewPrescriptionEntrySheet(viewEffect.patientUuid)
      is OpenDosagePickerSheet -> uiActions.showDosageSelectionSheet(
          viewEffect.drugName,
          viewEffect.patientUuid,
          viewEffect.prescribedDrugUuid
      )
      is ShowUpdateCustomPrescriptionSheet -> uiActions.showUpdateCustomPrescriptionSheet(viewEffect.prescribedDrug)
      GoBackToPatientSummary -> uiActions.goBackToPatientSummary()
    }
  }
}
