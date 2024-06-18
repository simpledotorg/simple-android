package org.simple.clinic.drugs

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class DiagnosisWarningPrescriptions(

    @Json(name = "htn_prescriptions")
    val htnPrescriptions: List<String>,

    @Json(name = "diabetes_prescriptions")
    val diabetesPrescriptions: List<String>
) : Parcelable {

  companion object {

    fun empty(): DiagnosisWarningPrescriptions {
      return DiagnosisWarningPrescriptions(
          htnPrescriptions = emptyList(),
          diabetesPrescriptions = emptyList()
      )
    }
  }
}
