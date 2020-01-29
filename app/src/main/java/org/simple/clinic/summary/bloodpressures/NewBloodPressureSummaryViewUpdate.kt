package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class NewBloodPressureSummaryViewUpdate : Update<NewBloodPressureSummaryViewModel, NewBloodPressureSummaryViewEvent, NewBloodPressureSummaryViewEffect> {
  override fun update(
      model: NewBloodPressureSummaryViewModel,
      event: NewBloodPressureSummaryViewEvent
  ): Next<NewBloodPressureSummaryViewModel, NewBloodPressureSummaryViewEffect> {
    return when (event) {
      is BloodPressuresLoaded -> next(model.bloodPressuresLoaded(event.measurements))
      is BloodPressuresCountLoaded -> next(model.bloodPressuresCountLoaded(event.count))
      is AddNewBloodPressureClicked -> dispatch(OpenBloodPressureEntrySheet(model.patientUuid))
      is BloodPressureClicked -> dispatch(OpenBloodPressureUpdateSheet(event.measurement))
      is SeeAllClicked -> dispatch(ShowBloodPressureHistoryScreen(model.patientUuid))
    }
  }
}
