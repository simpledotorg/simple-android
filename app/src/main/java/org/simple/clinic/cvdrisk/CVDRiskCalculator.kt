package org.simple.clinic.cvdrisk

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

object CVDRiskCalculator {

  fun calculateCvdRisk(
      cvdRiskInput: CVDRiskInput
  ): String? {
    with(cvdRiskInput) {
      if (cvdRiskData == null) return null

      val genderData = getGenderData(cvdRiskData, gender) ?: return null
      val smokingDataList = getSmokingDataList(genderData, isSmoker)
      val ageRange = getAgeRange(smokingDataList, age) ?: return null

      val sbpRange = getSBPRange(sbp)
      val bmiRangeList = getBMIRangeList(bmi)

      val risks = ageRange
          .filter { it.sbp == sbpRange && it.bmi in bmiRangeList }
          .map { it.risk }

      return formatRisk(risks)
    }
  }

  private fun getGenderData(cvdRiskData: CVDRiskCalculationSheet, gender: Gender) = when (gender) {
    Gender.Female -> cvdRiskData.women
    Gender.Male -> cvdRiskData.men
    else -> null
  }

  private fun getSmokingDataList(genderData: CVDRiskCalculationGenderSheet, isSmoker: Answer) = when (isSmoker) {
    Answer.Yes -> listOf(genderData.smoking)
    Answer.No -> listOf(genderData.nonSmoking)
    else -> listOf(genderData.nonSmoking, genderData.smoking)
  }

  private fun getAgeRange(smokingDataList: List<SmokingData>, age: Int): List<RiskEntry>? = when (age) {
    in 40..44 -> smokingDataList.mapNotNull { it.age40to44 }.flatten()
    in 45..49 -> smokingDataList.mapNotNull { it.age45to49 }.flatten()
    in 50..54 -> smokingDataList.mapNotNull { it.age50to54 }.flatten()
    in 55..59 -> smokingDataList.mapNotNull { it.age55to59 }.flatten()
    in 60..64 -> smokingDataList.mapNotNull { it.age60to64 }.flatten()
    in 65..69 -> smokingDataList.mapNotNull { it.age65to69 }.flatten()
    in 70..74 -> smokingDataList.mapNotNull { it.age70to74 }.flatten()
    else -> null
  }

  private fun getSBPRange(sbp: Int) = when (sbp) {
    in Int.MIN_VALUE..119 -> "120-"
    in 120..139 -> "120 - 139"
    in 140..159 -> "140 - 159"
    in 160..179 -> "160 - 179"
    else -> "180+"
  }

  private fun getBMIRangeList(bmi: Double?) = if (bmi != null) {
    listOf(
        when (bmi) {
          in Double.MIN_VALUE..19.9 -> "20-"
          in 20.0..24.9 -> "20 - 24"
          in 25.0..29.9 -> "25 - 29"
          in 30.0..34.9 -> "30 - 35"
          else -> "35+"
        }
    )
  } else {
    listOf("20-", "20 - 24", "25 - 29", "30 - 35", "35+")
  }

  private fun formatRisk(risks: List<Int>): String? = when {
    risks.isEmpty() -> null
    risks.size == 1 -> risks.first().toString()
    else -> "${risks.minOrNull()} - ${risks.maxOrNull()}"
  }
}
