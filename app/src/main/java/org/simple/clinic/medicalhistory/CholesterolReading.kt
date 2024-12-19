package org.simple.clinic.medicalhistory

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CholesterolReading(val value: String) : Parcelable
