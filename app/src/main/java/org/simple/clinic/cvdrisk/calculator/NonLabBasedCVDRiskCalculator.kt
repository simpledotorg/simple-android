package org.simple.clinic.cvdrisk.calculator

import dagger.Lazy
import org.simple.clinic.cvdrisk.CVDRiskInput
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.GenderData
import org.simple.clinic.cvdrisk.NonLabBasedRiskEntry
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.formatRisk
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.getAgeRange
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.getGenderData
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.getSmokingDataList
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.getSystolicRange
import org.simple.clinic.di.AppScope
import javax.inject.Inject

@AppScope
class NonLabBasedCVDRiskCalculator @Inject constructor(
    private val nonLabBasedCVDRiskCalculationSheet: Lazy<GenderData<NonLabBasedRiskEntry>?>,
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
      val sheet = nonLabBasedCVDRiskCalculationSheet.get()
      val genderData = sheet?.let { getGenderData(it, gender) }
      val smokingDataList = genderData?.let { getSmokingDataList(it, isSmoker) }
      return smokingDataList?.let { getAgeRange(smokingDataList, age) }
    }
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
