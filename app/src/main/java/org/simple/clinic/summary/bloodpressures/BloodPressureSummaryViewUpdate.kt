package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class BloodPressureSummaryViewUpdate : Update<BloodPressureSummaryViewModel, BloodPressureSummaryViewEvent, BloodPressureSummaryViewEffect> {
  override fun update(
      model: BloodPressureSummaryViewModel,
      event: BloodPressureSummaryViewEvent
  ): Next<BloodPressureSummaryViewModel, BloodPressureSummaryViewEffect> {
    return when (event) {
      is BloodPressuresLoaded -> next(model.bloodPressuresLoaded(event.measurements))
      is BloodPressuresCountLoaded -> next(model.bloodPressuresCountLoaded(event.count))
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
      is AddNewBloodPressureClicked -> dispatch(OpenBloodPressureEntrySheet(model.patientUuid))
      is BloodPressureClicked -> dispatch(OpenBloodPressureUpdateSheet(event.measurement))
      is SeeAllClicked -> dispatch(ShowBloodPressureHistoryScreen(model.patientUuid))
    }
  }
}
