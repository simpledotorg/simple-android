
package org.simple.clinic.navigation.v2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ScreenResult: Parcelable

@Parcelize
data class Succeeded(val result: Parcelable): ScreenResult()

@Parcelize
data class Failed(val cause: Parcelable? = null): ScreenResult()
