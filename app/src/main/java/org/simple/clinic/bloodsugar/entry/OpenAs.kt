package org.simple.clinic.bloodsugar.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import java.util.UUID

sealed class OpenAs(
    val measurementType: BloodSugarMeasurementType
) : Parcelable {

  @Parcelize
  data class New(val patientId: UUID, private val _measurementType: BloodSugarMeasurementType) : OpenAs(_measurementType)

  @Parcelize
  data class Update(val bloodSugarMeasurementUuid: UUID, private val _measurementType: BloodSugarMeasurementType) : OpenAs(_measurementType)
}
