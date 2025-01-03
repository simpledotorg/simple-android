package org.simple.clinic.patientattribute

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BMIReading(val height: Float, val weight: Float) : Parcelable
