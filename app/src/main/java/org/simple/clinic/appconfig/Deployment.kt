package org.simple.clinic.appconfig

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.net.URI

@Parcelize
@JsonClass(generateAdapter = true)
data class Deployment(
    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "endpoint")
    val endPoint: URI
) : Parcelable
