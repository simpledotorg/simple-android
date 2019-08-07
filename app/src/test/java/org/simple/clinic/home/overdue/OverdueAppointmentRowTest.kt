package org.simple.clinic.home.overdue

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

class OverdueAppointmentRowTest {

  private val userClock = TestUserClock(LocalDate.parse("2019-01-05"))

  @Test
  fun `overdue appointments must be converted to list items`() {
    // given
    val oneWeek = Duration.ofDays(7)
    val twoWeeks = Duration.ofDays(14)
    val oneYear = Duration.ofDays(365)

    val appointmentDelayedBy4Days = PatientMocker
        .overdueAppointment(
            name = "Anish Acharya",
            gender = Gender.Male,
            phoneNumber = PatientMocker.phoneNumber(number = "123456"),
            bloodPressureMeasurement = PatientMocker.bp(
                systolic = 127,
                diastolic = 95,
                recordedAt = Instant.now(userClock).minus(oneWeek)
            ),
            dateOfBirth = LocalDate.parse("1985-01-01"),
            age = null,
            appointment = PatientMocker.appointment(
                uuid = UUID.fromString("65d790f3-a9ea-4a83-bce1-8d1ea8539c67"),
                patientUuid = UUID.fromString("c88a4835-40e5-476b-9a6f-2f850c48ecdb"),
                scheduledDate = LocalDate.parse("2019-01-01")
            ),
            riskLevel = OverdueAppointment.RiskLevel.LOW
        )
    val appointmentDelayedByOneWeek = PatientMocker
        .overdueAppointment(
            name = "Deepa",
            gender = Gender.Female,
            phoneNumber = PatientMocker.phoneNumber(number = "45678912"),
            bloodPressureMeasurement = PatientMocker.bp(
                systolic = 168,
                diastolic = 110,
                recordedAt = Instant.now(userClock).minus(twoWeeks)
            ),
            dateOfBirth = null,
            age = PatientMocker.age(
                ageValue = 45,
                updatedAt = Instant.now(userClock).minus(oneYear)
            ),
            appointment = PatientMocker.appointment(
                uuid = UUID.fromString("4f13f6d3-05dc-4248-891b-b5ebd6f56987"),
                patientUuid = UUID.fromString("0c35a015-d823-4cc5-be77-21ce026c5780"),
                scheduledDate = LocalDate.parse("2018-12-29")
            ),
            riskLevel = OverdueAppointment.RiskLevel.HIGHEST
        )

    val appointments = listOf(appointmentDelayedBy4Days, appointmentDelayedByOneWeek)

    // when
    val overdueListItems = OverdueAppointmentRow.from(appointments, userClock)

    // then
    val expectedListItems = listOf(
        OverdueAppointmentRow(
            appointmentUuid = UUID.fromString("65d790f3-a9ea-4a83-bce1-8d1ea8539c67"),
            patientUuid = UUID.fromString("c88a4835-40e5-476b-9a6f-2f850c48ecdb"),
            name = "Anish Acharya",
            gender = Gender.Male,
            phoneNumber = "123456",
            bpSystolic = 127,
            bpDiastolic = 95,
            bpDaysAgo = 7,
            overdueDays = 4,
            isAtHighRisk = false,
            age = 34
        ),
        OverdueAppointmentRow(
            appointmentUuid = UUID.fromString("4f13f6d3-05dc-4248-891b-b5ebd6f56987"),
            patientUuid = UUID.fromString("0c35a015-d823-4cc5-be77-21ce026c5780"),
            name = "Deepa",
            gender = Gender.Female,
            phoneNumber = "45678912",
            bpSystolic = 168,
            bpDiastolic = 110,
            bpDaysAgo = 14,
            overdueDays = 7,
            isAtHighRisk = true,
            age = 46
        )
    )
    assertThat(overdueListItems)
        .containsExactlyElementsIn(expectedListItems)
        .inOrder()
  }
}
