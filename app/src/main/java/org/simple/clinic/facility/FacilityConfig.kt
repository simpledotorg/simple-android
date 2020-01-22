package org.simple.clinic.facility

import androidx.room.ColumnInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FacilityConfig(
    @ColumnInfo(name = "diabetesManagementEnabled")
    @Json(name = "enable_diabetes_management")
    val diabetesManagementEnabled: Boolean
)
