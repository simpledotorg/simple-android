package org.simple.clinic.facility

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class FacilityConfig(
    @ColumnInfo(name = "diabetesManagementEnabled")
    @Json(name = "enable_diabetes_management")
    val diabetesManagementEnabled: Boolean
): Parcelable
