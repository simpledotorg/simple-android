package org.simple.clinic.cvdrisk

import org.junit.Assert.assertEquals
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.cvdrisk.calculator.NonLabBasedCVDRiskCalculator
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

class NonLabBasedCVDRiskCalculatorTest {

  private val nonLabBasedCVDRiskCalculationSheet = TestData.nonLabBasedCVDRiskCalculationSheet()
  private val cvdRiskCalculator = NonLabBasedCVDRiskCalculator(
      nonLabBasedCVDRiskCalculationSheet = { nonLabBasedCVDRiskCalculationSheet }
  )

  @Test
  fun `should return exact risk for matching data`() {
    val cvdRiskInput = NonLabBasedCVDRiskInput(
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
    val cvdRiskInput = NonLabBasedCVDRiskInput(
        gender = Gender.Female,
        age = 40,
        systolic = 130,
        isSmoker = Answer.Yes,
        bmi = null
    )
    val risk = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(CVDRiskRange(5, 5), risk)
  }

  @Test
  fun `should return null when no matching data`() {
    val cvdRiskInput = NonLabBasedCVDRiskInput(
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
    val cvdRiskInput = NonLabBasedCVDRiskInput(
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
    val cvdRiskInput = NonLabBasedCVDRiskInput(
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
    val cvdRiskInput = NonLabBasedCVDRiskInput(
        gender = Gender.Male,
        age = 40,
        systolic = 125,
    )
    val risk = cvdRiskCalculator.calculateCvdRisk(cvdRiskInput)
    assertEquals(CVDRiskRange(3, 6), risk)
  }
}

