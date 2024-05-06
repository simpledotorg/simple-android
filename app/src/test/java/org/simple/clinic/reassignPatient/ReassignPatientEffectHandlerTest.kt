package org.simple.clinic.reassignPatient

import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.setup.UiActions
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.Optional
import java.util.UUID

class ReassignPatientEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val uiActions = mock<UiActions>()

  val patientUuid = UUID.fromString("0fa9cac4-2ca0-4d90-8588-e248ee882949")

  private val effectHandler = ReassignPatientEffectHandler(
      patientRepository = patientRepository,
      facilityRepository = facilityRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)


  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load assigned facility effect is received, then load assigned facility`() {
    // given
    val assignedFacilityUuid = UUID.fromString("28c84927-32f5-44c2-963f-5bf0ac4515b9")
    val patient = TestData.patient(
        uuid = patientUuid,
        registeredFacilityId = UUID.fromString("bc9eb873-4b75-4ef4-ad0f-60fe94187d7c"),
        assignedFacilityId = assignedFacilityUuid
    )
    val facility = TestData.facility(
        uuid = assignedFacilityUuid,
        name = "UHC Doha"
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient
    whenever(facilityRepository.facility(assignedFacilityUuid)) doReturn Optional.of(facility)

    // when
    effectHandlerTestCase.dispatch(LoadAssignedFacility(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AssignedFacilityLoaded(Optional.of(facility)))

    verifyNoInteractions(uiActions)
  }
}
