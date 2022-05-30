package org.simple.clinic.scheduleappointment

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.sharedTestCode.util.TestUserClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.sharedTestCode.uuid.FakeUuidGenerator
import java.time.LocalDate
import java.time.Period
import java.util.Optional
import java.util.UUID

class ScheduleAppointmentEffectHandlerTest {

  private val clock = TestUserClock(LocalDate.parse("2019-01-01"))
  private val patientUuid = UUID.fromString("78232434-0583-4377-8a61-fa5e7d899465")
  private val appointmentUuid = UUID.fromString("66168713-32b5-40e8-aa06-eb9821c3c141")
  private val teleconsultRecordUuid = UUID.fromString("78232434-0583-4377-8a61-fa5e7d896465")
  private val facility = TestData.facility()

  private val uiActions = mock<ScheduleAppointmentUiActions>()
  private val repository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val protocolRepository = mock<ProtocolRepository>()
  private val appointmentConfig: AppointmentConfig = AppointmentConfig(
      appointmentDuePeriodForDefaulters = Period.ofDays(30),
      scheduleAppointmentsIn = listOf(TimeToAppointment.Days(1)),
      defaultTimeToAppointment = TimeToAppointment.Days(1),
      periodForIncludingOverdueAppointments = Period.ofMonths(12),
      remindAppointmentsIn = emptyList()
  )
  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val viewEffectHandler = ScheduleAppointmentViewEffectHandler(uiActions)

  private val effectHandler = ScheduleAppointmentEffectHandler(
      currentFacility = Lazy { facility },
      protocolRepository = protocolRepository,
      appointmentRepository = repository,
      patientRepository = patientRepository,
      facilityRepository = facilityRepository,
      appointmentConfig = appointmentConfig,
      userClock = clock,
      schedulers = TrampolineSchedulersProvider(),
      uuidGenerator = FakeUuidGenerator.fixed(appointmentUuid),
      teleconsultRecordRepository = teleconsultRecordRepository,
      viewEffectsConsumer = viewEffectHandler::handle
  )
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load appointment facilities effect is received and patient has assigned facility, then load the appointment facilities`() {
    // given
    val assignedFacilityUuid = UUID.fromString("28977fb4-503a-45cb-995e-2a54188973e2")
    val patient = TestData.patient(
        uuid = patientUuid,
        assignedFacilityId = assignedFacilityUuid
    )
    val assignedFacility = TestData.facility(
        uuid = assignedFacilityUuid
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient
    whenever(facilityRepository.facility(assignedFacilityUuid)) doReturn Optional.of(assignedFacility)

    // when
    effectHandlerTestCase.dispatch(LoadAppointmentFacilities(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AppointmentFacilitiesLoaded(assignedFacility, facility))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load appointment facilities effect is received and patient doesn't have assigned facility, then load the appointment facilities`() {
    // given
    val patient = TestData.patient(
        uuid = patientUuid
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    effectHandlerTestCase.dispatch(LoadAppointmentFacilities(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AppointmentFacilitiesLoaded(null, facility))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load teleconsult record effect is loaded, then load the teleconsult record details`() {
    // given
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordUuid,
        patientId = patientUuid
    )

    whenever(teleconsultRecordRepository.getPatientTeleconsultRecord(patientUuid = patientUuid)) doReturn teleconsultRecord

    // when
    effectHandlerTestCase.dispatch(LoadTeleconsultRecord(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRecordLoaded(teleconsultRecord))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open teleconsult status sheet effect is received, then open teleconsult status sheet`() {
    // when
    effectHandlerTestCase.dispatch(GoToTeleconsultStatusSheet(teleconsultRecordUuid))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).openTeleconsultStatusSheet(teleconsultRecordUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when schedule appointment for patient from next effect is received, then mark older appointments as visited and schedule appointment`() {
    // given
    val scheduleDate = LocalDate.parse("2018-01-01")

    // when
    effectHandlerTestCase.dispatch(ScheduleAppointmentForPatientFromNext(
        patientUuid = patientUuid,
        scheduledForDate = scheduleDate,
        scheduledAtFacility = facility,
        type = Manual
    ))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AppointmentScheduledForPatientFromNext)

    verify(repository).markOlderAppointmentsAsVisited(patientUuid)
    verify(repository).schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = scheduleDate,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )
    verifyNoMoreInteractions(repository)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when schedule appointment for patient effect is received, then mark older appointments as visited and schedule appointment`() {
    // given
    val scheduleDate = LocalDate.parse("2018-01-01")

    // when
    effectHandlerTestCase.dispatch(ScheduleAppointmentForPatient(
        patientUuid = patientUuid,
        scheduledForDate = scheduleDate,
        scheduledAtFacility = facility,
        type = Automatic
    ))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AppointmentScheduled)

    verify(repository).markOlderAppointmentsAsVisited(patientUuid)
    verify(repository).schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = scheduleDate,
        appointmentType = Automatic,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )
    verifyNoMoreInteractions(repository)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when close sheet without result effect is received, then close the sheet without result`() {
    // when
    effectHandlerTestCase.dispatch(CloseSheetWithoutResult)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).closeSheetWithoutResult()
    verifyNoMoreInteractions(uiActions)
  }
}
