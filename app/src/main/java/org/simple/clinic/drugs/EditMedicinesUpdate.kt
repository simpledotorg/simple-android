package org.simple.clinic.drugs

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.drugs.EditMedicineButtonState.REFILL_MEDICINE
import org.simple.clinic.drugs.EditMedicineButtonState.SAVE_MEDICINE
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.util.toLocalDateAtZone
import java.time.LocalDate
import java.time.ZoneId

class EditMedicinesUpdate(
    private val currentDate: LocalDate,
    private val timeZone: ZoneId
) : Update<EditMedicinesModel, EditMedicinesEvent, EditMedicinesEffect> {

  override fun update(
      model: EditMedicinesModel,
      event: EditMedicinesEvent
  ): Next<EditMedicinesModel, EditMedicinesEffect> {
    return when (event) {
      AddNewPrescriptionClicked -> dispatch(ShowNewPrescriptionEntrySheet(model.patientUuid))
      is ProtocolDrugClicked -> dispatch(OpenDosagePickerSheet(event.drugName, model.patientUuid, event.prescription?.uuid))
      is CustomPrescriptionClicked -> dispatch(ShowUpdateCustomPrescriptionSheet(event.prescribedDrug))
      PrescribedDrugsDoneClicked -> dispatch(GoBackToPatientSummary)
      PresribedDrugsRefillClicked -> dispatch(RefillMedicines(model.patientUuid))
      is DrugsListFetched -> drugsListAndButtonStateFetched(event, model)
      PrescribedMedicinesRefilled -> dispatch(GoBackToPatientSummary)
    }
  }

  private fun drugsListAndButtonStateFetched(
      event: DrugsListFetched,
      model: EditMedicinesModel
  ): Next<EditMedicinesModel, EditMedicinesEffect> {
    val hasAnyPrescribedDrugBeenUpdatedToday = event.prescribedDrugs.any { prescribedDrug: PrescribedDrug ->
      prescribedDrug.updatedAt.toLocalDateAtZone(timeZone) == currentDate
    }
    val noPrescribedDrugsAvailable = event.prescribedDrugs.isNullOrEmpty()

    val hasAnyPrescribedDrugBeenDeleted = event.prescribedDrugs.any { prescribedDrug -> prescribedDrug.isDeleted }
    val isButtonStateSaveMedicine = hasAnyPrescribedDrugBeenUpdatedToday || hasAnyPrescribedDrugBeenDeleted || noPrescribedDrugsAvailable

    val buttonState = if (isButtonStateSaveMedicine) SAVE_MEDICINE else REFILL_MEDICINE

    val drugsFetchedAndSaveMedicineModel = model
        .prescribedDrugsFetched(event.prescribedDrugs)
        .protocolDrugsFetched(event.protocolDrugs)
        .editMedicineDrugStateFetched(buttonState)

    return next(drugsFetchedAndSaveMedicineModel)
  }
}
