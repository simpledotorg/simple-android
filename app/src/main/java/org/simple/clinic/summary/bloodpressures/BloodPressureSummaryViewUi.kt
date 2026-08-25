package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.medicalhistory.Answer

interface BloodPressureSummaryViewUi {
  fun showNoBloodPressuresView()
  fun showBloodPressures(
      bloodPressures: List<BloodPressureMeasurement>,
      diagnosedWithDiabetes: Answer?,
  )
  fun showSeeAllButton()
  fun hideSeeAllButton()
}
