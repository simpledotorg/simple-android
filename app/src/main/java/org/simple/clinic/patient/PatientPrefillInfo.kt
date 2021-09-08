package org.simple.clinic.patient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class PatientPrefillInfo(
    val fullName: String,
    val gender: Gender,
    val dateOfBirth: LocalDate,
    val address: String
) : Parcelable
