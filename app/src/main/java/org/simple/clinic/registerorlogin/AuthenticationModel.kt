package org.simple.clinic.registerorlogin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AuthenticationModel(val openFor: OpenFor): Parcelable
