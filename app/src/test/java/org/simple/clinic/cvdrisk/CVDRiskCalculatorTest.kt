package org.simple.clinic.cvdrisk

import org.junit.Assert.assertEquals
import org.junit.Test
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender
import org.simple.sharedTestCode.TestData

class CVDRiskCalculatorTest {

  private val cvdRiskCalculationSheet = TestData.cvdRiskCalculationSheet()
  private val cvdRiskCalculator = CVDRiskCalculator(
      cvdRiskCalculationSheet = { cvdRiskCalculationSheet }
  )

  @Test
  fun `should return exact risk for matching data`() {
    val cvdRiskInput = CVDRiskInput(
        gender = Gender.Female,
        age = 40,
        systolic = 130,
        isSmoker = Answer.Yes,
        bmi = 27f
    )

    val risk = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(CVDRiskRange(5, 5), risk)
  }

  @Test
  fun `should return risk range when bmi is not specified`() {
    val cvdRiskInput = CVDRiskInput(
        gender = Gender.Female,
        age = 40,
        systolic = 130,
        isSmoker = Answer.Yes,
        bmi = null
    )
    val risk = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(CVDRiskRange(5, 6), risk)
  }

  @Test
  fun `should return null when no matching data`() {
    val cvdRiskInput = CVDRiskInput(
        gender = Gender.Male,
        age = 80,
        systolic = 200,
        isSmoker = Answer.Yes,
        bmi = 40f
    )
    val risk = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(null, risk)
  }

  @Test
  fun `should handle nonsmoking data correctly`() {
    val cvdRiskInput = CVDRiskInput(
        gender = Gender.Male,
        age = 40,
        systolic = 125,
        isSmoker = Answer.No,
        bmi = 27f
    )
    val risk = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(CVDRiskRange(3, 3), risk)
  }

  @Test
  fun `should return risk range when smoking is unanswered`() {
    val cvdRiskInput = CVDRiskInput(
        gender = Gender.Male,
        age = 40,
        systolic = 125,
        isSmoker = Answer.Unanswered,
        bmi = 27f
    )
    val risk = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(CVDRiskRange(3, 6), risk)
  }

  @Test
  fun `should return risk range when bmi and smoking is not specified`() {
    val cvdRiskInput = CVDRiskInput(
        gender = Gender.Male,
        age = 40,
        systolic = 125,
    )
    val risk = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(CVDRiskRange(3, 8), risk)
  }
}

