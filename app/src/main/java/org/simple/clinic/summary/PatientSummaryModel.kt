package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class PatientSummaryModel(
    val patientUuid: UUID,
    val patientSummaryProfile: PatientSummaryProfile?
) : Parcelable {

  companion object {
    fun from(patientUuid: UUID): PatientSummaryModel {
      return PatientSummaryModel(patientUuid, null)
    }
  }

  fun patientSummaryProfileLoaded(patientSummaryProfile: PatientSummaryProfile): PatientSummaryModel {
    return copy(patientSummaryProfile = patientSummaryProfile)
  }
}
