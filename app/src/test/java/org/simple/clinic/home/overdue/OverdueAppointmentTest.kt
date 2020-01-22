package org.simple.clinic.home.overdue

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel
import org.simple.clinic.patient.PatientMocker

@RunWith(JUnitParamsRunner::class)
class OverdueAppointmentTest {

  @Test
  @Parameters(
      "0, HIGHEST",
      "5, NONE"
  )
  fun `should calculate risk level enum from indices correctly`(riskLevelIndex: Int, expectedRiskLevel: RiskLevel) {
    if (riskLevelIndex > 5) {
      throw AssertionError()
    }

    val overdueAppointment = PatientMocker.overdueAppointment(riskLevelIndex = riskLevelIndex)
    assertThat(overdueAppointment.riskLevel).isEqualTo(expectedRiskLevel)
  }

  @Test
  @Parameters(
      "HIGHEST, true",
      "NONE, false"
  )
  fun `should calculate if a patient is at risk correctly`(riskLevel: RiskLevel, isExpectedAtHighRisk: Boolean) {
    val overdueAppointment = PatientMocker.overdueAppointment(riskLevel = riskLevel)
    assertThat(overdueAppointment.isAtHighRisk).isEqualTo(isExpectedAtHighRisk)
  }
}
