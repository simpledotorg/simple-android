package org.simple.clinic.cvdrisk.calculator

import dagger.Lazy
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.DiabetesData
import org.simple.clinic.cvdrisk.LabBasedCVDRiskInput
import org.simple.clinic.cvdrisk.LabBasedRiskEntry
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.formatRisk
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.getAgeRange
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.getGenderData
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.getSmokingDataList
import org.simple.clinic.cvdrisk.calculator.CVDRiskCalculatorUtil.getSystolicRange
import org.simple.clinic.di.AppScope
import org.simple.clinic.medicalhistory.Answer
import javax.inject.Inject

@AppScope
class LabBasedCVDRiskCalculator @Inject constructor(
    private val labBasedCVDRiskCalculationSheet: Lazy<DiabetesData?>,
) {
    fun calculateCvdRisk(cvdRiskInput: LabBasedCVDRiskInput): CVDRiskRange? {
        with(cvdRiskInput) {
            val riskEntries = getLabBasedRiskEntries(cvdRiskInput) ?: return null

            val systolicRange = getSystolicRange(systolic)
            val cholesterolRangeList = getCholesterolRangeList(cholesterol)
            val risks =
                riskEntries.filter { it.systolic == systolicRange && it.cholesterol in cholesterolRangeList }
                    .map { it.risk }
            return formatRisk(risks)
        }
    }

    private fun getLabBasedRiskEntries(cvdRiskInput: LabBasedCVDRiskInput): List<LabBasedRiskEntry>? {
        with(cvdRiskInput) {
            val sheet = labBasedCVDRiskCalculationSheet.get()
            val diabetesData = sheet?.let { getDiabetesData(it, diagnosedWithDiabetes) }
            val genderData = diabetesData?.let { getGenderData(it, gender) }
            val smokingDataList = genderData?.let { getSmokingDataList(it, isSmoker) }
            return smokingDataList?.let { getAgeRange(smokingDataList, age) }
        }
    }

    private fun getDiabetesData(diabetesData: DiabetesData, answer: Answer) =
        when (answer) {
            Answer.Yes -> diabetesData.diabetes
            Answer.No -> diabetesData.noDiabetes
            else -> null
        }


    private fun getCholesterolRangeList(cholesterol: Float?): List<String> {
        return cholesterol?.let { listOf(getCholesterolRange(it)) }
            ?: listOf("< 4", "4 - 4.9", "5 - 5.9", "6 - 6.9", ">= 7")
    }

    private fun getCholesterolRange(cholesterol: Float): String {
        return when {
            cholesterol >= 0.0 && cholesterol < 4.0 -> "< 4"
            cholesterol >= 4.0 && cholesterol < 5.0 -> "4 - 4.9"
            cholesterol >= 5.0 && cholesterol < 6.0 -> "5 - 5.9"
            cholesterol >= 6.0 && cholesterol < 7.0 -> "6 - 6.9"
            else -> ">= 7"
        }
    }

}
