package org.simple.clinic.cvdrisk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patientattribute.BMIReading

@Parcelize
data class StatinInfo(
    val canPrescribeStatin: Boolean,
    val cvdRisk: CVDRiskRange? = null,
    val isSmoker: Answer = Answer.Unanswered,
    val bmiReading: BMIReading? = null,
) : Parcelable {
  companion object {
    fun default(): StatinInfo {
      return StatinInfo(
          canPrescribeStatin = false,
      )
    }
  }
}
