package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.drugs.OpenIntention
import org.simple.clinic.drugs.OpenIntention.RefillMedicine
import org.simple.clinic.mobius.dispatch

class TeleconsultMedicinesUpdate : Update<TeleconsultMedicinesModel, TeleconsultMedicinesEvent, TeleconsultMedicinesEffect> {

  override fun update(
      model: TeleconsultMedicinesModel,
      event: TeleconsultMedicinesEvent
  ): Next<TeleconsultMedicinesModel, TeleconsultMedicinesEffect> {
    return when (event) {
      is PatientMedicinesLoaded -> next(model.medicinesLoaded(event.medicines))
      EditMedicinesClicked -> {
        val openIntention = openIntentionForEditMedicineScreen()
        dispatch(OpenEditMedicines(model.patientUuid, openIntention))
      }
      is DrugDurationClicked -> dispatch(OpenDrugDurationSheet(event.prescription))
      is DrugFrequencyClicked -> dispatch(OpenDrugFrequencySheet(event.prescription))
      is DrugDurationChanged -> dispatch(UpdateDrugDuration(event.prescriptionUuid, event.duration))
      is DrugFrequencyChanged -> dispatch(UpdateDrugFrequency(event.prescriptionUuid, event.frequency))
    }
  }

  private fun openIntentionForEditMedicineScreen(): OpenIntention {
    return RefillMedicine
  }
}
