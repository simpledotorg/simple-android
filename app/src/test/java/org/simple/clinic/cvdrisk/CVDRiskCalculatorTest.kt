package org.simple.clinic.cvdrisk

import org.junit.Assert.assertEquals
import org.junit.Test
import org.simple.clinic.patient.Answer
import org.simple.clinic.patient.Gender
import org.simple.sharedTestCode.TestData

class CVDRiskCalculatorTest {

  private val testData = TestData.cvdRiskCalculationSheet()

  @Test
  fun `should return exact risk for matching data`() {
    val risk = CVDRiskCalculator.calculateCvdRisk(
        cvdRiskData = testData,
        gender = Gender.Female,
        age = 40,
        sbp = 130,
        isSmoker = Answer.Yes,
        bmi = 27.0
    )
    assertEquals("5", risk)
  }

  @Test
  fun `should return risk range when bmi is not specified`() {
    val risk = CVDRiskCalculator.calculateCvdRisk(
        cvdRiskData = testData,
        gender = Gender.Female,
        age = 40,
        sbp = 130,
        isSmoker = Answer.Yes,
        bmi = null
    )
    assertEquals("5 - 6", risk)
  }

  @Test
  fun `should return null when no matching data`() {
    val risk = CVDRiskCalculator.calculateCvdRisk(
        cvdRiskData = testData,
        gender = Gender.Male,
        age = 80,
        sbp = 200,
        isSmoker = Answer.Yes,
        bmi = 40.0
    )
    assertEquals(null, risk)
  }

  @Test
  fun `should handle nonsmoking data correctly`() {
    val risk = CVDRiskCalculator.calculateCvdRisk(
        cvdRiskData = testData,
        gender = Gender.Male,
        age = 40,
        sbp = 125,
        isSmoker = Answer.No,
        bmi = 27.0
    )
    assertEquals("3", risk)
  }

  @Test
  fun `should return risk range when smoking is unanswered`() {
    val risk = CVDRiskCalculator.calculateCvdRisk(
        cvdRiskData = testData,
        gender = Gender.Male,
        age = 40,
        sbp = 125,
        isSmoker = Answer.Unanswered,
        bmi = 27.0
    )
    assertEquals("3 - 6", risk)
  }

  @Test
  fun `should return risk range when bmi and smoking is not specified`() {
    val risk = CVDRiskCalculator.calculateCvdRisk(
        cvdRiskData = testData,
        gender = Gender.Male,
        age = 40,
        sbp = 125,
    )
    assertEquals("3 - 8", risk)
  }
}

