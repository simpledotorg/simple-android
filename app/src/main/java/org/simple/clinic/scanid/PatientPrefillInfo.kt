package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PatientPrefillInfo(
    val fullName: String,
    val gender: String,
    val dateOfBirth: String,
    val address: String
) : Parcelable
