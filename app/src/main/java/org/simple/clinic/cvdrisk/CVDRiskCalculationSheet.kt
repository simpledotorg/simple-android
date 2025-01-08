package org.simple.clinic.cvdrisk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CVDRiskCalculationSheet(
    val women: CVDRiskCategory,
    val men: CVDRiskCategory,
)

@JsonClass(generateAdapter = true)
data class CVDRiskCategory(
    val smoking: SmokingData,

    @Json(name = "nonsmoking")
    val nonSmoking: SmokingData
)

@JsonClass(generateAdapter = true)
data class SmokingData(
    @Json(name = "40 - 44") val age40to44: List<RiskEntry>?,
    @Json(name = "45 - 49") val age45to49: List<RiskEntry>?,
    @Json(name = "50 - 54") val age50to54: List<RiskEntry>?,
    @Json(name = "55 - 59") val age55to59: List<RiskEntry>?,
    @Json(name = "60 - 64") val age60to64: List<RiskEntry>?,
    @Json(name = "65 - 69") val age65to69: List<RiskEntry>?,
    @Json(name = "70 - 74") val age70to74: List<RiskEntry>?
)

@JsonClass(generateAdapter = true)
data class RiskEntry(
    val sbp: String,
    val bmi: String,
    val risk: Int
)
