package org.simple.clinic.navigation

import android.os.Parcelable
import androidx.annotation.LayoutRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BackStackEntry(
    @LayoutRes val layoutRes: Int,
    val analyticsName: String
) : Parcelable
