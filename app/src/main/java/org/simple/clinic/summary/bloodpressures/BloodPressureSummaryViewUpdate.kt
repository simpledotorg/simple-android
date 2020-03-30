package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import java.util.UUID

class BloodPressureSummaryViewUpdate(
    private val config: BloodPressureSummaryViewConfig
) : Update<BloodPressureSummaryViewModel, BloodPressureSummaryViewEvent, BloodPressureSummaryViewEffect> {
  override fun update(
      model: BloodPressureSummaryViewModel,
      event: BloodPressureSummaryViewEvent
  ): Next<BloodPressureSummaryViewModel, BloodPressureSummaryViewEffect> {
    return when (event) {
      is BloodPressuresLoaded -> next(model.bloodPressuresLoaded(event.measurements))
      is BloodPressuresCountLoaded -> next(model.bloodPressuresCountLoaded(event.count))
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility), loadBloodPressures(model.patientUuid, event.facility.config))
      is AddNewBloodPressureClicked -> dispatch(ShouldShowFacilityChangeAlert)
      is BloodPressureClicked -> dispatch(OpenBloodPressureUpdateSheet(event.measurement))
      is SeeAllClicked -> dispatch(ShowBloodPressureHistoryScreen(model.patientUuid))
      is ShowFacilityChangeAlert -> {
        val effect = if (event.showFacilityChangeAlert) OpenAlertFacilityChangeSheet(model.facility!!) else OpenBloodPressureEntrySheet(model.patientUuid)
        dispatch(effect)
      }
    }
  }

  private fun loadBloodPressures(
      patientUuid: UUID,
      facilityConfig: FacilityConfig
  ): LoadBloodPressures {
    val numberOfBpsToDisplay = if (facilityConfig.diabetesManagementEnabled) {
      config.numberOfBpsToDisplay
    } else {
      config.numberOfBpsToDisplayWithoutDiabetesManagement
    }

    return LoadBloodPressures(patientUuid, numberOfBpsToDisplay)
  }
}
