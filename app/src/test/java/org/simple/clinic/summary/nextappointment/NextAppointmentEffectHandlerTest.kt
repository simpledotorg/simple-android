package org.simple.clinic.summary.nextappointment

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientAndAssignedFacility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class NextAppointmentEffectHandlerTest {

  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val effectHandler = NextAppointmentEffectHandler(
      appointmentRepository = appointmentRepository,
      patientRepository = patientRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)
  private val patientUuid = UUID.fromString("06ffb32b-fc59-4e38-9f08-9be810b313da")

  @After
  fun teardown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load appointment effect is received, then load the appointment`() {
    // given
    val appointmentUuid = UUID.fromString("13dea42d-1958-412e-9db7-6f7601373245")
    val appointment = TestData.appointment(
        uuid = appointmentUuid,
        patientUuid = patientUuid
    )

    whenever(appointmentRepository.latestAppointmentForPatient(patientUuid)) doReturn Observable.just(appointment)

    // when
    effectHandlerTestCase.dispatch(LoadAppointment(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AppointmentLoaded(appointment))
  }

  @Test
  fun `when load patient and assigned facility effect is received, then load the patient and assigned facility`() {
    // given
    val assignedFacility = TestData.facility(
        uuid = UUID.fromString("76af00b5-8e20-4cc8-bf7a-a48cd710fed9"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = UUID.fromString("1bbcd62d-80de-4d7f-a0e1-f8283f0d5670"),
        fullName = "Ramesh Mehta",
        assignedFacilityId = assignedFacility.uuid
    )

    val patientAndAssignedFacility = PatientAndAssignedFacility(patient, assignedFacility)

    whenever(patientRepository.patientAndAssignedFacility(patientUuid)) doReturn Observable.just(patientAndAssignedFacility)

    // when
    effectHandlerTestCase.dispatch(LoadPatientAndAssignedFacility(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientAndAssignedFacilityLoaded(patientAndAssignedFacility))
  }
}
