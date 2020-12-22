package org.simple.clinic.navigation.v2

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class NavRequest(val key: ScreenKey) : Parcelable

@Parcelize
data class Normal(private val _key: ScreenKey) : NavRequest(_key)

@Parcelize
data class ExpectingResult(
    val requestType: Parcelable,
    private val _key: ScreenKey
) : NavRequest(_key)
