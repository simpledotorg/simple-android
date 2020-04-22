package org.simple.clinic.drugs

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class EditMedicinesUpdate : Update<EditMedicinesModel, EditMedicinesEvent, EditMedicinesEffect> {

  override fun update(model: EditMedicinesModel, event: EditMedicinesEvent): Next<EditMedicinesModel, EditMedicinesEffect> {
    return when (event) {
      AddNewPrescriptionClicked -> dispatch(ShowNewPrescriptionEntrySheet(model.patientUuid))
      is ProtocolDrugClicked -> dispatch(OpenDosagePickerSheet(event.drugName, model.patientUuid, event.prescriptionForProtocolDrug?.uuid))
    }
  }
}
