package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TimerDuration(val minutes: String, val seconds: String): Parcelable
