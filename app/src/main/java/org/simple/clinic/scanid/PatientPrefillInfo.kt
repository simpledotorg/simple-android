package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.Gender
import java.time.LocalDate

@Parcelize
data class PatientPrefillInfo(
    val fullName: String,
    val gender: Gender,
    val dateOfBirth: LocalDate,
    val address: String
) : Parcelable
