package org.simple.clinic.bp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BloodPressureReading(
    val systolic: Int,
    val diastolic: Int
) : Parcelable
