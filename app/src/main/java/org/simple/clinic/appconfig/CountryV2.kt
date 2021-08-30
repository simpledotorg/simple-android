package org.simple.clinic.appconfig

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CountryV2(

    @Json(name = "country_code")
    val isoCountryCode: String,

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "isd_code")
    val isdCode: String,

    @Json(name = "deployments")
    val deployments: List<Deployment>
) : Parcelable
