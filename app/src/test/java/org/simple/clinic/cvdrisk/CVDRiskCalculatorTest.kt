package org.simple.clinic.cvdrisk

import org.junit.Assert.assertEquals
import org.junit.Test
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender
import org.simple.sharedTestCode.TestData

class CVDRiskCalculatorTest {

  private val testData = TestData.cvdRiskCalculationSheet()

  @Test
  fun `should return exact risk for matching data`() {
    val cvdRiskInput = CVDRiskInput(
        cvdRiskData = testData,
        gender = Gender.Female,
        age = 40,
        sbp = 130,
        isSmoker = Answer.Yes,
        bmi = 27.0
    )

    val risk = CVDRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals("5", risk)
  }

  @Test
  fun `should return risk range when bmi is not specified`() {
    val cvdRiskInput = CVDRiskInput(
        cvdRiskData = testData,
        gender = Gender.Female,
        age = 40,
        sbp = 130,
        isSmoker = Answer.Yes,
        bmi = null
    )
    val risk = CVDRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals("5 - 6", risk)
  }

  @Test
  fun `should return null when no matching data`() {
    val cvdRiskInput = CVDRiskInput(
        cvdRiskData = testData,
        gender = Gender.Male,
        age = 80,
        sbp = 200,
        isSmoker = Answer.Yes,
        bmi = 40.0
    )
    val risk = CVDRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(null, risk)
  }

  @Test
  fun `should handle nonsmoking data correctly`() {
    val cvdRiskInput = CVDRiskInput(
        cvdRiskData = testData,
        gender = Gender.Male,
        age = 40,
        sbp = 125,
        isSmoker = Answer.No,
        bmi = 27.0
    )
    val risk = CVDRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals("3", risk)
  }

  @Test
  fun `should return risk range when smoking is unanswered`() {
    val cvdRiskInput = CVDRiskInput(
        cvdRiskData = testData,
        gender = Gender.Male,
        age = 40,
        sbp = 125,
        isSmoker = Answer.Unanswered,
        bmi = 27.0
    )
    val risk = CVDRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals("3 - 6", risk)
  }

  @Test
  fun `should return risk range when bmi and smoking is not specified`() {
    val cvdRiskInput = CVDRiskInput(
        cvdRiskData = testData,
        gender = Gender.Male,
        age = 40,
        sbp = 125,
    )
    val risk = CVDRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals("3 - 8", risk)
  }
}

