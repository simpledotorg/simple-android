package org.simple.clinic.teleconsultlog.drugduration

interface DrugDurationUi {
  fun showBlankDurationError()
  fun hideDurationError()
  fun showMaxDrugDurationError(maxAllowedDurationInDays: Int)
}
