package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurement

interface BloodSugarSummaryViewUi {
  fun showBloodSugarSummary(bloodSugars: List<BloodSugarMeasurement>)
  fun showNoBloodSugarsView()
  fun showSeeAllButton()
  fun hideSeeAllButton()
}
