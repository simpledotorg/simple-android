package org.simple.clinic.drugs

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiagnosisWarningPrescriptions(

    @Json(name = "htn_prescriptions")
    val htnPrescriptions: List<String>,

    @Json(name = "diabetes_prescriptions")
    val diabetesPrescriptions: List<String>
) {

  companion object {

    fun empty(): DiagnosisWarningPrescriptions {
      return DiagnosisWarningPrescriptions(
          htnPrescriptions = emptyList(),
          diabetesPrescriptions = emptyList()
      )
    }
  }
}
