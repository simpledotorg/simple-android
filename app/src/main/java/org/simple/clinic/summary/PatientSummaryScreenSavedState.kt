package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PatientSummaryScreenSavedState(val superSavedState: Parcelable, val bpEntryShownOnStart: Boolean) : Parcelable
