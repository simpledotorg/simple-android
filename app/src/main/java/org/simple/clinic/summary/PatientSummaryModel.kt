package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class PatientSummaryModel(val patientUuid: UUID) : Parcelable {

  companion object {
    fun from(patientUuid: UUID): PatientSummaryModel {
      return PatientSummaryModel(patientUuid)
    }
  }
}
