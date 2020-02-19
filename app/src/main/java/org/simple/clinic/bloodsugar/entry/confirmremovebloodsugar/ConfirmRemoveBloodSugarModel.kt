package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class ConfirmRemoveBloodSugarModel(val bloodSugarMeasurementUuid: UUID) : Parcelable {
  companion object {
    fun create(bloodSugarMeasurementUuid: UUID) = ConfirmRemoveBloodSugarModel(bloodSugarMeasurementUuid)
  }
}
