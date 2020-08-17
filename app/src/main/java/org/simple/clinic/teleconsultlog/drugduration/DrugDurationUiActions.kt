package org.simple.clinic.teleconsultlog.drugduration

interface DrugDurationUiActions {
  fun showBlankDurationError()
  fun hideDurationError()
  fun saveDrugDuration(duration: Int)
}
