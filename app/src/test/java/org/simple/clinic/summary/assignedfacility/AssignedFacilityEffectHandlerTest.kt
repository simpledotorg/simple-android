package org.simple.clinic.summary.assignedfacility

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class AssignedFacilityEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()

  private val effectHandler = AssignedFacilityEffectHandler(
      patientRepository = patientRepository,
      facilityRepository = facilityRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load assigned facility effect is received, then load assigned facility`() {
    // given
    val assignedFacilityUuid = UUID.fromString("0ef45f88-21a1-4620-ae4d-94395bbbbb87")
    val patientUuid = UUID.fromString("17bb9690-9a17-4d90-a45f-8bfdcc4153e4")
    val patient = TestData.patient(
        uuid = patientUuid,
        registeredFacilityId = UUID.fromString("7e101cd0-572c-401d-a1a5-ab5f9a925ad2"),
        assignedFacilityId = assignedFacilityUuid
    )
    val facility = TestData.facility(
        uuid = assignedFacilityUuid,
        name = "CHC Obvious"
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient
    whenever(facilityRepository.facility(assignedFacilityUuid)) doReturn Optional.of(facility)

    // when
    effectHandlerTestCase.dispatch(LoadAssignedFacility(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AssignedFacilityLoaded(Optional.of(facility)))
  }
}
