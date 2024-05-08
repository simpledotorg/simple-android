package org.simple.clinic.reassignpatient

import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.Optional
import java.util.UUID

class ReassignPatientEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val uiActions = mock<ReassignPatientUiActions>()
  private val viewEffectHandler = ReassignPatientViewEffectHandler(uiActions)

  val patientUuid = UUID.fromString("0fa9cac4-2ca0-4d90-8588-e248ee882949")

  private val effectHandler = ReassignPatientEffectHandler(
      patientRepository = patientRepository,
      facilityRepository = facilityRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = viewEffectHandler::handle
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

  @Test
  fun `when close sheet effect is received , then close sheet`() {
    // when
    effectHandlerTestCase.dispatch(CloseSheet(ReassignPatientSheetClosedFrom.NOT_NOW))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).closeSheet(ReassignPatientSheetClosedFrom.NOT_NOW)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when select facility sheet effect is received , then open select facility sheet`() {
    // when
    effectHandlerTestCase.dispatch(OpenSelectFacilitySheet)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openSelectFacilitySheet()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when change assigned facility effect is received, then change the assigned facility`() {
    // given
    val patientUuid = UUID.fromString("163d66b6-b7a4-47f3-8aea-257636489956")
    val updatedAssignedFacilityId = UUID.fromString("bf9ff0b1-c859-4ff9-9dfe-544e5fbc11d3")

    // when
    effectHandlerTestCase.dispatch(ChangeAssignedFacility(patientUuid, updatedAssignedFacilityId))

    // then
    verify(patientRepository).updateAssignedFacilityId(
        patientId = patientUuid,
        assignedFacilityId = updatedAssignedFacilityId
    )

    effectHandlerTestCase.assertOutgoingEvents(AssignedFacilityChanged)
    verifyNoInteractions(uiActions)
  }
}
