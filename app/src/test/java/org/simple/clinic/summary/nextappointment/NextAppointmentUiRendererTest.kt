package org.simple.clinic.summary.nextappointment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class NextAppointmentUiRendererTest {

  private val ui = mock<NextAppointmentUi>()
  private val uiRenderer = NextAppointmentUiRenderer(ui)
  private val patientUuid = UUID.fromString("305c6c33-90b5-4895-9f82-e6d071d05955")
  private val defaultModel = NextAppointmentModel.default(
      patientUuid = patientUuid,
      currentDate = LocalDate.parse("2018-01-01")
  )

  @Test
  fun `when next appointment patient profile is not present, then render no appointment view`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showNoAppointment()
    verify(ui).showAddAppointmentButton()
    verify(ui).hideAppointmentFacility()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when appointment is scheduled for current date, then show appointment date`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("d5ab9b31-101c-4172-a50a-6c57b79a3712"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Ramesh",
        assignedFacilityId = facility.uuid
    )

    val appointment = TestData.appointment(
        uuid = UUID.fromString("01361f22-c10e-465d-97de-c44f990572c4"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        scheduledDate = LocalDate.parse("2018-01-01")
    )

    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(appointment, patient, facility)

    val nextAppointmentPatientProfileLoadedModel = defaultModel
        .nextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile)

    // when
    uiRenderer.render(nextAppointmentPatientProfileLoadedModel)

    // then
    verify(ui).showAppointmentDate(LocalDate.parse("2018-01-01"))
    verify(ui).showChangeAppointmentButton()
    verify(ui).hideAppointmentFacility()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when appointment schedule date is before current date, then show appointment date with remaining days`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("d5ab9b31-101c-4172-a50a-6c57b79a3712"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Ramesh",
        assignedFacilityId = facility.uuid
    )

    val appointment = TestData.appointment(
        uuid = UUID.fromString("01361f22-c10e-465d-97de-c44f990572c4"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        scheduledDate = LocalDate.parse("2018-01-05")
    )

    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(appointment, patient, facility)

    val nextAppointmentPatientProfileLoadedModel = defaultModel
        .nextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile)

    // when
    uiRenderer.render(nextAppointmentPatientProfileLoadedModel)

    // then
    verify(ui).showAppointmentDateWithRemainingDays(date = LocalDate.parse("2018-01-05"), daysRemaining = 4)
    verify(ui).showChangeAppointmentButton()
    verify(ui).hideAppointmentFacility()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when appointment schedule date is after current date, then show appointment date with overdue days`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("d5ab9b31-101c-4172-a50a-6c57b79a3712"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Ramesh",
        assignedFacilityId = facility.uuid
    )

    val appointment = TestData.appointment(
        uuid = UUID.fromString("01361f22-c10e-465d-97de-c44f990572c4"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        scheduledDate = LocalDate.parse("2018-01-05")
    )

    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(appointment, patient, facility)

    val nextAppointmentPatientProfileLoadedModel = NextAppointmentModel
        .default(
            patientUuid = patientUuid,
            currentDate = LocalDate.parse("2018-01-10")
        )
        .nextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile)

    // when
    uiRenderer.render(nextAppointmentPatientProfileLoadedModel)

    // then
    verify(ui).showAppointmentDateWithOverdueDays(date = LocalDate.parse("2018-01-05"), overdueDays = 5)
    verify(ui).showChangeAppointmentButton()
    verify(ui).hideAppointmentFacility()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when appointment facility is different from assigned facility, then show appointment facility`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("d5ab9b31-101c-4172-a50a-6c57b79a3712"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Ramesh",
        assignedFacilityId = UUID.fromString("fa63232f-0755-4c13-969e-e7d5da3b78d0")
    )

    val appointment = TestData.appointment(
        uuid = UUID.fromString("01361f22-c10e-465d-97de-c44f990572c4"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        scheduledDate = LocalDate.parse("2018-01-01")
    )

    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(appointment, patient, facility)

    val nextAppointmentPatientProfileLoadedModel = defaultModel
        .nextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile)

    // when
    uiRenderer.render(nextAppointmentPatientProfileLoadedModel)

    // then
    verify(ui).showAppointmentDate(LocalDate.parse("2018-01-01"))
    verify(ui).showChangeAppointmentButton()
    verify(ui).showAppointmentFacility("PHC Obvious")
    verifyNoMoreInteractions(ui)
  }
}
