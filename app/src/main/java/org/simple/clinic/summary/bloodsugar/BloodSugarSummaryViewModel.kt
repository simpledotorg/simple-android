package org.simple.clinic.summary.bloodsugar

class BloodSugarSummaryViewModel {
  companion object {
    val EMPTY = BloodSugarSummaryViewModel()
  }

  fun summaryFetched(): BloodSugarSummaryViewModel {
    return EMPTY
  }
}
