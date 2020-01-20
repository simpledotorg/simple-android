package org.simple.clinic.bp.history

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
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
      is BloodPressureHistoryLoaded -> next(model.historyLoaded(event.bloodPressures))
      is NewBloodPressureClicked -> dispatch(OpenBloodPressureEntrySheet)
      is BloodPressureClicked -> dispatch(OpenBloodPressureUpdateSheet(event.bloodPressureMeasurement))
    }
  }
}
