package org.simple.clinic.teleconsultlog.drugduration

interface DrugDurationUi {
  fun showBlankDurationError()
  fun hideDurationError()
  fun setDrugDuration(duration: String?)
}
