package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface DiagnosisWarningResult : Parcelable {

  @Parcelize
  data object HypertensionWarning : DiagnosisWarningResult

  @Parcelize
  data object DiabetesWarning : DiagnosisWarningResult

  @Parcelize
  data object BothDiagnosisWarning : DiagnosisWarningResult
}
