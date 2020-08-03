package org.simple.clinic.summary.prescribeddrugs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class DrugSummaryModel(
    val patientUuid: UUID
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID) = DrugSummaryModel(patientUuid)
  }
}
