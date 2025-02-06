package org.simple.clinic.cvdrisk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class DiabetesData(
    val diabetes: GenderData<LabBasedRiskEntry>,
    @Json(name = "nodiabetes") val noDiabetes: GenderData<LabBasedRiskEntry>
)

@JsonClass(generateAdapter = true)
class GenderData<T : RiskEntry>(
    val women: SmokingData<T>,
    val men: SmokingData<T>
)

@JsonClass(generateAdapter = true)
data class SmokingData<T : RiskEntry>(
    val smoking: AgeData<T>,

    @Json(name = "nonsmoking")
    val nonSmoking: AgeData<T>
)

@JsonClass(generateAdapter = true)
data class AgeData<T : RiskEntry>(
    @Json(name = "40 - 44") val age40to44: List<T>?,
    @Json(name = "45 - 49") val age45to49: List<T>?,
    @Json(name = "50 - 54") val age50to54: List<T>?,
    @Json(name = "55 - 59") val age55to59: List<T>?,
    @Json(name = "60 - 64") val age60to64: List<T>?,
    @Json(name = "65 - 69") val age65to69: List<T>?,
    @Json(name = "70 - 74") val age70to74: List<T>?
)

interface RiskEntry

@JsonClass(generateAdapter = true)
data class LabBasedRiskEntry(
    @Json(name = "sbp") val systolic: String,
    @Json(name = "chol") val cholesterol: String,
    val risk: Int
) : RiskEntry

@JsonClass(generateAdapter = true)
data class NonLabBasedRiskEntry(
    @Json(name = "sbp") val systolic: String,
    val bmi: String,
    val risk: Int
) : RiskEntry
