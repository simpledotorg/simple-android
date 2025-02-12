package org.simple.clinic.cvdrisk

import dagger.Lazy
import org.simple.clinic.di.AppScope
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender
import javax.inject.Inject

@AppScope
class CVDRiskCalculator @Inject constructor(
    private val nonLabBasedCVDRiskCalculationSheet: Lazy<NonLabBasedCVDRiskCalculationSheet?>,
) {
  fun calculateCvdRisk(cvdRiskInput: CVDRiskInput): CVDRiskRange? {
    with(cvdRiskInput) {
      val riskEntries = getNonLabBasedRiskEntries(cvdRiskInput) ?: return null

      val systolicRange = getSystolicRange(systolic)
      val bmiRangeList = getBMIRangeList(bmi)
      val risks = riskEntries.filter { it.systolic == systolicRange && it.bmi in bmiRangeList }.map { it.risk }
      return formatRisk(risks)
    }
  }

  private fun getNonLabBasedRiskEntries(cvdRiskInput: CVDRiskInput): List<NonLabBasedRiskEntry>? {
    with(cvdRiskInput) {
      val sheet = nonLabBasedCVDRiskCalculationSheet.get() ?: return null
      val genderData = getGenderData(sheet, gender)
      val smokingDataList = genderData?.let { getSmokingDataList(it, isSmoker) }
      return smokingDataList?.let { getAgeRange(smokingDataList, age) }
    }
  }

  private fun getGenderData(cvdRiskData: NonLabBasedCVDRiskCalculationSheet, gender: Gender) = when (gender) {
    Gender.Female -> cvdRiskData.women
    Gender.Male -> cvdRiskData.men
    else -> null
  }

  private fun <T : RiskEntry> getSmokingDataList(smokingData: SmokingData<T>, isSmoker: Answer) = when (isSmoker) {
    Answer.Yes -> listOf(smokingData.smoking)
    Answer.No -> listOf(smokingData.nonSmoking)
    else -> listOf(smokingData.nonSmoking, smokingData.smoking)
  }

  private fun <T : RiskEntry> getAgeRange(ageData: List<AgeData<T>>, age: Int): List<T>? {
    val ageToRiskMapping = mapOf(
        40..44 to { data: AgeData<T> -> data.age40to44 },
        45..49 to { data: AgeData<T> -> data.age45to49 },
        50..54 to { data: AgeData<T> -> data.age50to54 },
        55..59 to { data: AgeData<T> -> data.age55to59 },
        60..64 to { data: AgeData<T> -> data.age60to64 },
        65..69 to { data: AgeData<T> -> data.age65to69 },
        70..74 to { data: AgeData<T> -> data.age70to74 }
    )

    val riskExtractor = ageToRiskMapping.entries
        .firstOrNull { age in it.key }
        ?.value ?: return null

    return ageData.map(riskExtractor).flatten()
  }

  private fun getSystolicRange(sbp: Int) = when (sbp) {
    in 0..159 -> "140 - 159"
    in 160..179 -> "160 - 179"
    else -> ">= 180"
  }

  private fun getBMIRangeList(bmi: Float?): List<String> {
    return bmi?.let { listOf(getBMIRange(it)) }
        ?: listOf("< 20", "20 - 24", "25 - 29", "30 - 35", "> 35")
  }

  private fun getBMIRange(bmi: Float): String {
    return when (bmi) {
      in 0.0..19.9 -> "< 20"
      in 20.0..24.9 -> "20 - 24"
      in 25.0..29.9 -> "25 - 29"
      in 30.0..34.9 -> "30 - 35"
      else -> "> 35"
    }
  }

  private fun formatRisk(risks: List<Int>): CVDRiskRange? {
    return when {
      risks.isEmpty() -> null
      risks.size == 1 -> CVDRiskRange(risks.first(), risks.first())
      else -> CVDRiskRange(risks.min(), risks.max())
    }
  }
}
