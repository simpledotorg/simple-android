package org.simple.clinic.summary.teleconsultation.api

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class TeleconsultPhoneNumber(
    @Json(name = "phone_number")
    val phoneNumber: String
) : Parcelable
