package org.simple.clinic.cvdrisk

import org.junit.Assert.assertEquals
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.cvdrisk.calculator.LabBasedCVDRiskCalculator
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

class LabBasedCVDRiskCalculatorTest {

  private val labBasedCVDRiskCalculationSheet = TestData.labBasedCVDRiskCalculationSheet()
  private val cvdRiskCalculator = LabBasedCVDRiskCalculator(
      labBasedCVDRiskCalculationSheet = { labBasedCVDRiskCalculationSheet }
  )

  @Test
  fun `should return exact risk for matching data`() {
    val expectedRiskRange = CVDRiskRange(7, 7)
    val cvdRiskInput = LabBasedCVDRiskInput(
        gender = Gender.Female,
        age = 40,
        systolic = 130,
        isSmoker = Answer.Yes,
        diagnosedWithDiabetes = Answer.Yes,
        cholesterol = 3f
    )
    val riskRange = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)

    assertEquals(expectedRiskRange, riskRange)
  }

  @Test
  fun `should return risk range when cholesterol is not specified`() {
    val expectedRiskRange = CVDRiskRange(7, 13)
    val cvdRiskInput = LabBasedCVDRiskInput(
        gender = Gender.Female,
        age = 40,
        systolic = 130,
        isSmoker = Answer.Yes,
        diagnosedWithDiabetes = Answer.Yes,
    )
    val riskRange = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)

    assertEquals(expectedRiskRange, riskRange)
  }

  @Test
  fun `should return null when no matching data`() {
    val cvdRiskInput = LabBasedCVDRiskInput(
        gender = Gender.Male,
        age = 80,
        systolic = 200,
        isSmoker = Answer.Yes,
        cholesterol = 5.9f,
        diagnosedWithDiabetes = Answer.Yes,
    )
    val riskRange = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)

    assertEquals(null, riskRange)
  }

  @Test
  fun `should handle nonsmoking data correctly`() {
    val expectedRiskRange = CVDRiskRange(4, 4)
    val cvdRiskInput = LabBasedCVDRiskInput(
        gender = Gender.Male,
        age = 40,
        systolic = 125,
        isSmoker = Answer.No,
        cholesterol = 5.9f,
        diagnosedWithDiabetes = Answer.Yes,
    )
    val riskRange = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)

    assertEquals(expectedRiskRange, riskRange)
  }

  @Test
  fun `should return risk range when smoking is unanswered`() {
    val expectedRiskRange = CVDRiskRange(4, 7)
    val cvdRiskInput = LabBasedCVDRiskInput(
        gender = Gender.Male,
        age = 40,
        systolic = 125,
        isSmoker = Answer.Unanswered,
        cholesterol = 3.4f,
        diagnosedWithDiabetes = Answer.Yes,
    )
    val riskRange = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)

    assertEquals(expectedRiskRange, riskRange)
  }

  @Test
  fun `should return risk range when cholesterol and smoking is not specified`() {
    val expectedRiskRange = CVDRiskRange(4, 14)
    val cvdRiskInput = LabBasedCVDRiskInput(
        gender = Gender.Male,
        age = 40,
        systolic = 125,
        diagnosedWithDiabetes = Answer.Yes,
    )
    val riskRange = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)

    assertEquals(expectedRiskRange, riskRange)
  }
}
