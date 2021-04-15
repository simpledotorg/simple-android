package org.simple.clinic.bp.entry.confirmremovebloodpressure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ConfirmRemoveBloodPressureModel(
    val bloodPressureMeasurementUuid: UUID
) : Parcelable {

  companion object {
    fun create(bloodPressureMeasurementUuid: UUID) = ConfirmRemoveBloodPressureModel(
        bloodPressureMeasurementUuid = bloodPressureMeasurementUuid
    )
  }
}
