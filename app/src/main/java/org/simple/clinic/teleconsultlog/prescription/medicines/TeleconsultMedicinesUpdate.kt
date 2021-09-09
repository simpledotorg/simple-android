package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency

class TeleconsultMedicinesUpdate : Update<TeleconsultMedicinesModel, TeleconsultMedicinesEvent, TeleconsultMedicinesEffect> {

  override fun update(
      model: TeleconsultMedicinesModel,
      event: TeleconsultMedicinesEvent
  ): Next<TeleconsultMedicinesModel, TeleconsultMedicinesEffect> {
    return when (event) {
      is PatientMedicinesLoaded -> next(model.medicinesLoaded(event.medicines))
      EditMedicinesClicked -> {
        dispatch(OpenEditMedicines(model.patientUuid))
      }
      is DrugDurationClicked -> dispatch(OpenDrugDurationSheet(event.prescription))
      is DrugFrequencyClicked -> drugFrequencyClicked(event, model)
      is DrugDurationChanged -> dispatch(UpdateDrugDuration(event.prescriptionUuid, event.duration))
      is DrugFrequencyChanged -> dispatch(UpdateDrugFrequency(event.prescriptionUuid, event.frequency))
      is DrugFrequencyChoiceItemsLoaded -> drugFrequencyChoiceItemsLoaded(model, event)
    }
  }

  private fun drugFrequencyClicked(event: DrugFrequencyClicked, model: TeleconsultMedicinesModel): Next<TeleconsultMedicinesModel, TeleconsultMedicinesEffect> {
    return dispatch(OpenDrugFrequencySheet(event.prescription, model.medicineFrequencyToFrequencyChoiceItemMap))
  }

  private fun drugFrequencyChoiceItemsLoaded(
      model: TeleconsultMedicinesModel,
      event: DrugFrequencyChoiceItemsLoaded
  ): Next<TeleconsultMedicinesModel, TeleconsultMedicinesEffect> {
    val medicineFrequencyToDrugFrequencyChoiceItemMap =
        event.drugFrequencyChoiceItems.items
            .associateBy({ MedicineFrequency.fromDrugFrequency(it.drugFrequency) }, { it })
    return next(model.medicineFrequencyToFrequencyChoiceItemMapLoaded(medicineFrequencyToDrugFrequencyChoiceItemMap))
  }
}
