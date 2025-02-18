package org.simple.clinic.cvdrisk.calculator

import dagger.Lazy
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.LabBasedCVDRiskCalculationSheet
import org.simple.clinic.cvdrisk.LabBasedCVDRiskInput
import org.simple.clinic.cvdrisk.LabBasedRiskEntry
import org.simple.clinic.di.AppScope
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender
import javax.inject.Inject

@AppScope
class LabBasedCVDRiskCalculator @Inject constructor(
    private val labBasedCVDRiskCalculationSheet: Lazy<LabBasedCVDRiskCalculationSheet?>,
) {

  fun calculateCvdRisk(cvdRiskInput: LabBasedCVDRiskInput): CVDRiskRange? {
    with(cvdRiskInput) {
      val riskEntries = getLabBasedRiskEntries(cvdRiskInput) ?: return null
      val cholesterolRangeList = getCholesterolRangeList(cholesterol)
      val risks =
          riskEntries
              .filter { it.isInSystolicRange(systolic) && it.cholesterol in cholesterolRangeList }
              .map { it.risk }

      return CVDRiskRange.from(risks)
    }
  }

  private fun getLabBasedRiskEntries(cvdRiskInput: LabBasedCVDRiskInput): List<LabBasedRiskEntry>? {
    with(cvdRiskInput) {
      val sheet = labBasedCVDRiskCalculationSheet.get()
      val diabetesData = sheet?.let { getDiabetesData(it, diagnosedWithDiabetes) }
      val genderData = diabetesData?.let { getGenderData(it, gender) }
      val smokingDataList = genderData?.ageDataForSmokingStatus(isSmoker)

      return smokingDataList?.flatMap { it.riskForAge(age) }
    }
  }

  private fun getGenderData(genderData: LabBasedCVDRiskCalculationSheet.DiabetesRisk, gender: Gender) = when (gender) {
    Gender.Female -> genderData.women
    Gender.Male -> genderData.men
    else -> null
  }

  private fun getDiabetesData(diabetesData: LabBasedCVDRiskCalculationSheet, answer: Answer) =
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
