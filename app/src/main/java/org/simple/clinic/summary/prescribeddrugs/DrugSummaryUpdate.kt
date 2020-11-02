package org.simple.clinic.summary.prescribeddrugs

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.drugs.OpenIntention
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class DrugSummaryUpdate : Update<DrugSummaryModel, DrugSummaryEvent, DrugSummaryEffect> {
  override fun update(model: DrugSummaryModel, event: DrugSummaryEvent): Next<DrugSummaryModel, DrugSummaryEffect> {
    return when (event) {
      is PatientSummaryUpdateDrugsClicked -> dispatch(LoadCurrentFacility)
      is CurrentFacilityLoaded -> {
        val openIntention = openIntentionForEditMedicineScreen(model)

        dispatch(OpenUpdatePrescribedDrugScreen(model.patientUuid, event.facility, openIntention))
      }
      is PrescribedDrugsLoaded -> next(model.prescribedDrugsLoaded(event.prescribedDrugs))
    }
  }

  private fun openIntentionForEditMedicineScreen(model: DrugSummaryModel): OpenIntention {
    return if (model.prescribedDrugsNotNullorEmpty) {
      OpenIntention.RefillMedicine  
    } else {
      OpenIntention.AddNewMedicine
    }
  }
}
