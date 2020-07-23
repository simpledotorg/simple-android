package org.simple.clinic.home.overdue

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.util.TestUserClock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class OverdueAppointmentRowTest {

  private val userClock = TestUserClock(LocalDate.parse("2019-01-05"))
  private val dateFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH)

  @Test
  fun `overdue appointments must be converted to list items`() {
    // given
    val oneYear = Duration.ofDays(365)
    val patientAddress1 = TestData.overduePatientAddress()
    val patientAddress2 = TestData.overduePatientAddress()

    val facilityUuid = UUID.fromString("e3cd5735-2ef2-4777-a977-3a42e5a7b03b")
    val patientAssignedFacilityId = facilityUuid

    val appointmentDelayedBy4Days = TestData
        .overdueAppointment(
            name = "Anish Acharya",
            isHighRisk = false,
            gender = Gender.Male,
            dateOfBirth = LocalDate.parse("1985-01-01"),
            age = null,
            phoneNumber = TestData.patientPhoneNumber(number = "123456"),
            appointment = TestData.appointment(
                uuid = UUID.fromString("65d790f3-a9ea-4a83-bce1-8d1ea8539c67"),
                patientUuid = UUID.fromString("c88a4835-40e5-476b-9a6f-2f850c48ecdb"),
                scheduledDate = LocalDate.parse("2019-01-01")
            ),
            patientLastSeen = Instant.parse("2020-01-01T00:00:00Z"),
            diagnosedWithDiabetes = Yes,
            diagnosedWithHypertension = No,
            patientAddress = patientAddress1,
            patientAssignedFacilityId = patientAssignedFacilityId,
            appointmentFacilityName = "PHC Obvious"
        )
    val appointmentDelayedByOneWeek = TestData
        .overdueAppointment(
            name = "Deepa",
            isHighRisk = true,
            gender = Gender.Female,
            dateOfBirth = null,
            age = Age(45, Instant.now(userClock).minus(oneYear)),
            phoneNumber = TestData.patientPhoneNumber(number = "45678912"),
            appointment = TestData.appointment(
                uuid = UUID.fromString("4f13f6d3-05dc-4248-891b-b5ebd6f56987"),
                patientUuid = UUID.fromString("0c35a015-d823-4cc5-be77-21ce026c5780"),
                scheduledDate = LocalDate.parse("2018-12-29"),
                facilityUuid = facilityUuid
            ),
            patientLastSeen = Instant.parse("2019-12-25T00:00:00Z"),
            diagnosedWithDiabetes = No,
            diagnosedWithHypertension = null,
            patientAddress = patientAddress2,
            patientAssignedFacilityId = patientAssignedFacilityId,
            appointmentFacilityName = "PHC Bagta"
        )

    val appointments = listOf(appointmentDelayedBy4Days, appointmentDelayedByOneWeek)

    // when
    val overdueListItems = OverdueAppointmentRow.from(appointments, userClock, dateFormatter, isDiabetesManagementEnabled = true)

    // then
    val expectedListItems = listOf(
        OverdueAppointmentRow(
            appointmentUuid = UUID.fromString("65d790f3-a9ea-4a83-bce1-8d1ea8539c67"),
            patientUuid = UUID.fromString("c88a4835-40e5-476b-9a6f-2f850c48ecdb"),
            name = "Anish Acharya",
            gender = Gender.Male,
            age = 34,
            phoneNumber = "123456",
            overdueDays = 4,
            isAtHighRisk = false,
            lastSeenDate = "1-Jan-2020",
            diagnosedWithDiabetes = Yes,
            diagnosedWithHypertension = No,
            showDiagnosisLabel = true,
            patientAddress = patientAddress1,
            isAppointmentAtAssignedFacility = false,
            appointmentFacilityName = "PHC Obvious"
        ),
        OverdueAppointmentRow(
            appointmentUuid = UUID.fromString("4f13f6d3-05dc-4248-891b-b5ebd6f56987"),
            patientUuid = UUID.fromString("0c35a015-d823-4cc5-be77-21ce026c5780"),
            name = "Deepa",
            gender = Gender.Female,
            age = 46,
            phoneNumber = "45678912",
            overdueDays = 7,
            isAtHighRisk = true,
            lastSeenDate = "25-Dec-2019",
            diagnosedWithDiabetes = No,
            diagnosedWithHypertension = null,
            showDiagnosisLabel = true,
            patientAddress = patientAddress2,
            isAppointmentAtAssignedFacility = true,
            appointmentFacilityName = "PHC Bagta"
        )
    )
    assertThat(overdueListItems)
        .containsExactlyElementsIn(expectedListItems)
        .inOrder()
  }
}
