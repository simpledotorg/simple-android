package org.simple.clinic.cvdrisk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.medicalhistory.Answer

@JsonClass(generateAdapter = true)
data class LabBasedCVDRiskCalculationSheet(
    val diabetes: DiabetesRisk,
    @Json(name = "nodiabetes") val noDiabetes: DiabetesRisk,
) : CVDRiskCalculationSheet {

  @JsonClass(generateAdapter = true)
  data class DiabetesRisk(
      val women: Women<LabBasedRiskEntry>,
      val men: Men<LabBasedRiskEntry>
  )
}

@JsonClass(generateAdapter = true)
data class NonLabBasedCVDRiskCalculationSheet(
    val women: Women<NonLabBasedRiskEntry>,
    val men: Men<NonLabBasedRiskEntry>
) : CVDRiskCalculationSheet

sealed interface CVDRiskCalculationSheet

@JsonClass(generateAdapter = true)
data class Women<T : RiskEntry>(
    override val smoking: AgeData<T>,
    @Json(name = "nonsmoking")
    override val nonSmoking: AgeData<T>
) : SmokingData<T>

@JsonClass(generateAdapter = true)
data class Men<T : RiskEntry>(
    override val smoking: AgeData<T>,
    @Json(name = "nonsmoking")
    override val nonSmoking: AgeData<T>
) : SmokingData<T>

sealed interface SmokingData<T : RiskEntry> {
  val smoking: AgeData<T>
  val nonSmoking: AgeData<T>

  fun ageDataForSmokingStatus(isSmoker: Answer): List<AgeData<T>> {
    return when (isSmoker) {
      Answer.Yes -> listOf(smoking)
      Answer.No -> listOf(nonSmoking)
      else -> listOf(nonSmoking, smoking)
    }
  }
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
) {

  fun riskForAge(age: Int): List<T> {
    return when (age) {
      in 40..44 -> age40to44
      in 45..49 -> age45to49
      in 50..54 -> age50to54
      in 55..59 -> age55to59
      in 60..64 -> age60to64
      in 65..69 -> age65to69
      in 70..74 -> age70to74
      else -> emptyList()
    }
  }
}

@JsonClass(generateAdapter = true)
data class LabBasedRiskEntry(
    @Json(name = "sbp") override val systolic: String,
    @Json(name = "chol") val cholesterol: String,
    val risk: Int
) : RiskEntry

@JsonClass(generateAdapter = true)
data class NonLabBasedRiskEntry(
    @Json(name = "sbp") override val systolic: String,
    val bmi: String,
    val risk: Int
) : RiskEntry

sealed interface RiskEntry {

  val systolic: String

  fun isInSystolicRange(systolic: Int): Boolean {
    val systolicRange = when (systolic) {
      in 0..159 -> "140 - 159"
      in 160..179 -> "160 - 179"
      else -> ">= 180"
    }

    return this.systolic == systolicRange
  }
}
