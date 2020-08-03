package org.simple.clinic.summary.prescribeddrugs

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class DrugSummaryUpdate : Update<DrugSummaryModel, DrugSummaryEvent, DrugSummaryEffect> {
  override fun update(model: DrugSummaryModel, event: DrugSummaryEvent): Next<DrugSummaryModel, DrugSummaryEffect> {
    return when (event) {
      is PatientSummaryUpdateDrugsClicked -> dispatch(LoadCurrentFacility)
      is CurrentFacilityLoaded -> dispatch(OpenUpdatePrescribedDrugScreen(model.patientUuid, event.facility))
      is PrescribedDrugsLoaded -> next(model.prescribedDrugsLoaded(event.prescribedDrugs))
    }
  }
}
