package org.simple.clinic.bloodsugar.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import java.util.UUID

sealed class OpenAs : Parcelable

@Parcelize
data class New(val patientId: UUID, val measurementType: BloodSugarMeasurementType) : OpenAs()

@Parcelize
data class Update(val bloodSugarMeasurementUuid: UUID) : OpenAs()
