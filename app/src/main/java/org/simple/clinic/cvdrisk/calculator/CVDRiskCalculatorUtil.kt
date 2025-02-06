package org.simple.clinic.cvdrisk.calculator

import org.simple.clinic.cvdrisk.AgeData
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.GenderData
import org.simple.clinic.cvdrisk.RiskEntry
import org.simple.clinic.cvdrisk.SmokingData
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

object CVDRiskCalculatorUtil {
  fun <T : RiskEntry> getGenderData(genderData: GenderData<T>, gender: Gender) = when (gender) {
    Gender.Female -> genderData.women
    Gender.Male -> genderData.men
    else -> null
  }

  fun <T : RiskEntry> getSmokingDataList(smokingData: SmokingData<T>, isSmoker: Answer) = when (isSmoker) {
    Answer.Yes -> listOf(smokingData.smoking)
    Answer.No -> listOf(smokingData.nonSmoking)
    else -> listOf(smokingData.nonSmoking, smokingData.smoking)
  }

  fun <T : RiskEntry> getAgeRange(ageData: List<AgeData<T>>, age: Int): List<T>? {
    val ageToRiskMapping = mapOf(
        40..44 to { data: AgeData<T> -> data.age40to44 },
        45..49 to { data: AgeData<T> -> data.age45to49 },
        50..54 to { data: AgeData<T> -> data.age50to54 },
        55..59 to { data: AgeData<T> -> data.age55to59 },
        60..64 to { data: AgeData<T> -> data.age60to64 },
        65..69 to { data: AgeData<T> -> data.age65to69 },
        70..74 to { data: AgeData<T> -> data.age70to74 }
    )

    val riskExtractor = ageToRiskMapping.entries.firstOrNull { age in it.key }?.value ?: return null
    return ageData.mapNotNull(riskExtractor).flatten()
  }

  fun getSystolicRange(sbp: Int) = when (sbp) {
    in 0..159 -> "140 - 159"
    in 160..179 -> "160 - 179"
    else -> ">= 180"
  }

  fun formatRisk(risks: List<Int>): CVDRiskRange? {
    return when {
      risks.isEmpty() -> null
      risks.size == 1 -> CVDRiskRange(risks.first(), risks.first())
      else -> CVDRiskRange(risks.minOrNull() ?: return null, risks.maxOrNull() ?: return null)
    }
  }
}
