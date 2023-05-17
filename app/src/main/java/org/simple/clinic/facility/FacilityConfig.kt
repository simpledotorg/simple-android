package org.simple.clinic.facility

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class FacilityConfig(
    @ColumnInfo(name = "diabetesManagementEnabled")
    @Json(name = "enable_diabetes_management")
    val diabetesManagementEnabled: Boolean,

    // TODO (SM): Make this non nullable and remove default value once the feature is in PROD
    @ColumnInfo(name = "teleconsultationEnabled")
    @Json(name = "enable_teleconsultation")
    val teleconsultationEnabled: Boolean? = false,

    // TODO (SA): Make this non nullable and remove default value once the feature is in PROD
    @ColumnInfo(name = "monthlyScreeningReportsEnabled")
    @Json(name = "enable_monthly_screening_reports")
    val monthlyScreeningReportsEnabled: Boolean? = false,

    @ColumnInfo(name = "monthlySuppliesReportsEnabled")
    @Json(name = "enable_monthly_supplies_reports")
    val monthlySuppliesReportsEnabled: Boolean,
) : Parcelable
