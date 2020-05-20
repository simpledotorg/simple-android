package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OverduePatientAddress(
    val streetAddress: String?,
    val colonyOrVillage: String?,
    val district: String,
    val state: String
) : Parcelable
