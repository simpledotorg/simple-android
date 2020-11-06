package org.simple.clinic.drugs

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class EditMedicinesUpdate : Update<EditMedicinesModel, EditMedicinesEvent, EditMedicinesEffect> {

  override fun update(model: EditMedicinesModel, event: EditMedicinesEvent): Next<EditMedicinesModel, EditMedicinesEffect> {
    return when (event) {
      AddNewPrescriptionClicked -> dispatch(ShowNewPrescriptionEntrySheet(model.patientUuid))
      is ProtocolDrugClicked -> dispatch(OpenDosagePickerSheet(event.drugName, model.patientUuid, event.prescription?.uuid))
      is CustomPrescriptionClicked -> dispatch(ShowUpdateCustomPrescriptionSheet(event.prescribedDrug))
      PrescribedDrugsDoneClicked -> dispatch(GoBackToPatientSummary)
      is DrugsListFetched -> next(model.prescribedDrugsFetched(event.prescribedDrugs).protocolDrugsFetched(event.protocolDrugs))
      PrescribedMedicinesRefilled -> dispatch(GoBackToPatientSummary)
    }
  }
}
