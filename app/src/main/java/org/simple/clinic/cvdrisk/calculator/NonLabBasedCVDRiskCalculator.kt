package org.simple.clinic.cvdrisk.calculator

import dagger.Lazy
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.NonLabBasedCVDRiskCalculationSheet
import org.simple.clinic.cvdrisk.NonLabBasedCVDRiskInput
import org.simple.clinic.cvdrisk.NonLabBasedRiskEntry
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.Gender
import javax.inject.Inject

@AppScope
class NonLabBasedCVDRiskCalculator @Inject constructor(
    private val nonLabBasedCVDRiskCalculationSheet: Lazy<NonLabBasedCVDRiskCalculationSheet?>,
) {
  fun calculateCvdRisk(cvdRiskInput: NonLabBasedCVDRiskInput): CVDRiskRange? {
    with(cvdRiskInput) {
      val riskEntries = getNonLabBasedRiskEntries(cvdRiskInput) ?: return null
      val bmiRangeList = getBMIRangeList(bmi)
      val risks = riskEntries
          .filter { it.isInSystolicRange(systolic) && it.bmi in bmiRangeList }
          .map { it.risk }

      return CVDRiskRange.from(risks)
    }
  }

  private fun getNonLabBasedRiskEntries(cvdRiskInput: NonLabBasedCVDRiskInput): List<NonLabBasedRiskEntry>? {
    with(cvdRiskInput) {
      val sheet = nonLabBasedCVDRiskCalculationSheet.get()
      val genderData = sheet?.let { getGenderData(it, gender) }
      val smokingDataList = genderData?.ageDataForSmokingStatus(isSmoker)

      return smokingDataList?.flatMap { it.riskForAge(age) }
    }
  }

  private fun getGenderData(genderData: NonLabBasedCVDRiskCalculationSheet, gender: Gender) = when (gender) {
    Gender.Female -> genderData.women
    Gender.Male -> genderData.men
    else -> null
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
}
