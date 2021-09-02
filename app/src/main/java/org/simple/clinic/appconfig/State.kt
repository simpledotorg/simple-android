package org.simple.clinic.appconfig

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class State(

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "deployment")
    val deployment: Deployment
) : Parcelable
