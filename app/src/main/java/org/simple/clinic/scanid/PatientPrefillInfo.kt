package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class PatientPrefillInfo(
    val fullName: String,
    val gender: String,
    val dateOfBirth: LocalDate,
    val address: String
) : Parcelable
