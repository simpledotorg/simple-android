package org.simple.clinic.bloodsugar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BloodSugarReading(val value: Int, val type: BloodSugarMeasurementType) : Parcelable
