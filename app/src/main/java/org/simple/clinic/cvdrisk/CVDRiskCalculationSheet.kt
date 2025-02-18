package org.simple.clinic.cvdrisk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LabBasedCVDRiskCalculationSheet(
    val diabetes: DiabetesRisk,
    @Json(name = "nodiabetes") val noDiabetes: NoDiabetesRisk,
) : CVDRiskCalculationSheet {

  @JsonClass(generateAdapter = true)
  data class DiabetesRisk(
      val women: Women<LabBasedRiskEntry>,
      val men: Men<LabBasedRiskEntry>
  )

  @JsonClass(generateAdapter = true)
  data class NoDiabetesRisk(
      val women: Women<LabBasedRiskEntry>,
      val men: Men<LabBasedRiskEntry>
  )
}

@JsonClass(generateAdapter = true)
data class NonLabBasedCVDRiskCalculationSheet(
    val women: Women<NonLabBasedRiskEntry>,
    val men: Men<NonLabBasedRiskEntry>
): CVDRiskCalculationSheet

sealed interface CVDRiskCalculationSheet

@JsonClass(generateAdapter = true)
data class Women<T: RiskEntry>(
    override val smoking: AgeData<T>,
    @Json(name = "nonsmoking")
    override val nonSmoking: AgeData<T>
): SmokingData<T>

@JsonClass(generateAdapter = true)
data class Men<T: RiskEntry>(
    override val smoking: AgeData<T>,
    @Json(name = "nonsmoking")
    override val nonSmoking: AgeData<T>
): SmokingData<T>

sealed interface SmokingData<T: RiskEntry> {
  val smoking: AgeData<T>
  val nonSmoking: AgeData<T>
}

@JsonClass(generateAdapter = true)
data class AgeData<T : RiskEntry>(
    @Json(name = "40 - 44") val age40to44: List<T>,
    @Json(name = "45 - 49") val age45to49: List<T>,
    @Json(name = "50 - 54") val age50to54: List<T>,
    @Json(name = "55 - 59") val age55to59: List<T>,
    @Json(name = "60 - 64") val age60to64: List<T>,
    @Json(name = "65 - 69") val age65to69: List<T>,
    @Json(name = "70 - 74") val age70to74: List<T>
)

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

sealed interface RiskEntry
