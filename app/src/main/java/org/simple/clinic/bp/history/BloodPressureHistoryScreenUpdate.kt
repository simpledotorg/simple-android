package org.simple.clinic.bp.history

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class BloodPressureHistoryScreenUpdate : Update<BloodPressureHistoryScreenModel, BloodPressureHistoryScreenEvent, BloodPressureHistoryScreenEffect> {
  override fun update(
      model: BloodPressureHistoryScreenModel,
      event: BloodPressureHistoryScreenEvent
  ): Next<BloodPressureHistoryScreenModel, BloodPressureHistoryScreenEffect> {
    return when (event) {
      is PatientLoaded -> next(model.patientLoaded(event.patient))
      is NewBloodPressureClicked -> dispatch(OpenBloodPressureEntrySheet(model.patientUuid))
      is BloodPressureClicked -> dispatch(OpenBloodPressureUpdateSheet(event.bloodPressureMeasurement))
    }
  }
}
