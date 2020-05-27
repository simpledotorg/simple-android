package org.simple.clinic.editpatient.deletepatient

import androidx.annotation.StringRes
import org.simple.clinic.R

enum class PatientDeleteReason(@StringRes val displayText: Int) {
  Duplicate(R.string.deletereason_duplicate_patient),
  AccidentalRegistration(R.string.deletereason_accidental_registration),
  Died(R.string.deletereason_died)
}