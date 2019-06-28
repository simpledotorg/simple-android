package org.simple.clinic.patient

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

@Parcelize
data class Age(
    val value: Int,
    val updatedAt: Instant,
    @Deprecated(message = "This property is no longer in use")
    val computedDateOfBirth: LocalDate
) : Parcelable
