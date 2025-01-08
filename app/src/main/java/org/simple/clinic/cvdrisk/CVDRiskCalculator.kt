package org.simple.clinic.cvdrisk

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

object CVDRiskCalculator {

  fun calculateCvdRisk(cvdRiskInput: CVDRiskInput): String? {
    with(cvdRiskInput) {
      val riskEntries = getRiskEntries(cvdRiskInput) ?: return null

      val systolicRange = getSystolicRange(systolic)
      val bmiRangeList = getBMIRangeList(bmi)
      val risks = riskEntries.filter { it.systolic == systolicRange && it.bmi in bmiRangeList }.map { it.risk }
      return formatRisk(risks)
    }
  }

  private fun getRiskEntries(cvdRiskInput: CVDRiskInput): List<RiskEntry>? {
    with(cvdRiskInput) {
      val genderData = cvdRiskData?.let { getGenderData(it, gender) }
      val smokingDataList = genderData?.let { getSmokingDataList(it, isSmoker) }
      return smokingDataList?.let { getAgeRange(smokingDataList, age) }
    }
  }

  private fun getGenderData(cvdRiskData: CVDRiskCalculationSheet, gender: Gender) = when (gender) {
    Gender.Female -> cvdRiskData.women
    Gender.Male -> cvdRiskData.men
    else -> null
  }

  private fun getSmokingDataList(genderData: CVDRiskCategory, isSmoker: Answer) = when (isSmoker) {
    Answer.Yes -> listOf(genderData.smoking)
    Answer.No -> listOf(genderData.nonSmoking)
    else -> listOf(genderData.nonSmoking, genderData.smoking)
  }

  private fun getAgeRange(smokingDataList: List<SmokingData>, age: Int): List<RiskEntry>? {
    val ageToRiskMapping = mapOf(
        40..44 to { data: SmokingData -> data.age40to44 },
        45..49 to { data: SmokingData -> data.age45to49 },
        50..54 to { data: SmokingData -> data.age50to54 },
        55..59 to { data: SmokingData -> data.age55to59 },
        60..64 to { data: SmokingData -> data.age60to64 },
        65..69 to { data: SmokingData -> data.age65to69 },
        70..74 to { data: SmokingData -> data.age70to74 }
    )

    val riskExtractor = ageToRiskMapping.entries
        .firstOrNull { age in it.key }
        ?.value ?: return null

    return smokingDataList.mapNotNull(riskExtractor).flatten()
  }

  private fun getSystolicRange(sbp: Int) = when (sbp) {
    in 0..119 -> "120-"
    in 120..139 -> "120 - 139"
    in 140..159 -> "140 - 159"
    in 160..179 -> "160 - 179"
    else -> "180+"
  }

  private fun getBMIRangeList(bmi: Double?): List<String> {
    return bmi?.let { listOf(getBMIRange(it)) }
        ?: listOf("20-", "20 - 24", "25 - 29", "30 - 35", "35+")
  }

  private fun getBMIRange(bmi: Double): String {
    return when (bmi) {
      in Double.MIN_VALUE..19.9 -> "20-"
      in 20.0..24.9 -> "20 - 24"
      in 25.0..29.9 -> "25 - 29"
      in 30.0..34.9 -> "30 - 35"
      else -> "35+"
    }
  }

  private fun formatRisk(risks: List<Int>): String? {
    return when {
      risks.isEmpty() -> null
      risks.size == 1 -> risks.first().toString()
      else -> "${risks.minOrNull()} - ${risks.maxOrNull()}"
    }
  }
}
